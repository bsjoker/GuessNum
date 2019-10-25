package com.funapps.gje

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.truelove.kotlinplatinarush.model.PreferencesHelper
import kotlinx.android.synthetic.main.activity_webview.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MyWebView : AppCompatActivity() {
    companion object {
        const val TAG = "LoadActivity"
    }

    val INPUT_FILE_REQUEST_CODE = 1
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var mCameraPhotoPath: String? = null
    private lateinit var mUrl: String
    //private lateinit var webView: WebView
    private lateinit var myRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        initFirebase()

        myRef.child("url").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value: String? = dataSnapshot.getValue(String::class.java)
                Log.d(TAG, "${value} result from db")

                val change1sub = value?.replace(
                    "sub1",
                    PreferencesHelper().getSharedPreferences().getString("sub1", "")
                )
                mUrl = change1sub.toString().replace(
                    "sub2",
                    PreferencesHelper().getSharedPreferences().getString("sub2", "")
                )

                webView.loadUrl(mUrl)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException())
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        }

        val webSettings = webView.getSettings()
        webSettings.setJavaScriptEnabled(true)
        webSettings.setDomStorageEnabled(true)
        webSettings.setLoadWithOverviewMode(true)
        webSettings.setUseWideViewPort(true)
        webSettings.setBuiltInZoomControls(true)
        webSettings.setDisplayZoomControls(false)
        webSettings.setSupportZoom(true)
        webSettings.setDefaultTextEncodingName("utf-8")
        webSettings.setAllowFileAccess(true)
        webSettings.setSupportMultipleWindows(true)
        webView.getSettings().setPluginState(WebSettings.PluginState.ON)

        webView.setWebViewClient(MyWebViewClient())
        webView.setWebChromeClient(
            object : WebChromeClient() {
                override fun onShowFileChooser(
                    webView: WebView, filePathCallback: ValueCallback<Array<Uri>>,
                    fileChooserParams: WebChromeClient.FileChooserParams
                ): Boolean {
                    if (mFilePathCallback != null) {
                        mFilePathCallback!!.onReceiveValue(null)
                    }
                    mFilePathCallback = filePathCallback

                    var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePictureIntent!!.resolveActivity(getPackageManager()) != null) {
                        // Create the File where the photo should go
                        var photoFile: File? = null
                        try {
                            photoFile = createImageFile()
                            takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath)
                        } catch (ex: IOException) {
                            // Error occurred while creating the File
                            Log.e(TAG, "Unable to create Image File", ex)
                        }

                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            mCameraPhotoPath = "file:" + photoFile.absolutePath
                            takePictureIntent.putExtra(
                                MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile)
                            )
                        } else {
                            takePictureIntent = null
                        }
                    }

                    val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                    contentSelectionIntent.type = "image/*"

                    val intentArray: Array<Intent?>
                    if (takePictureIntent != null) {
                        intentArray = arrayOf(takePictureIntent)
                    } else {
                        intentArray = arrayOfNulls(0)
                    }

                    val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)

                    startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE)

                    return true
                }
            })

        webView.loadUrl(PreferencesHelper().getSharedPreferences().getString("url", ""))
    }

    /**
     * More info this method can be found at
     * http://developer.android.com/training/camera/photobasics.html
     *
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        return File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
        )
    }

    /**
     * Convenience method to set some generic defaults for a
     * given WebView
     *
     * @param webView
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun setUpWebViewDefaults(webView: WebView) {
        val settings = webView.settings

        // Enable Javascript
        settings.javaScriptEnabled = true

        // Use WideViewport and Zoom out if there is no viewport defined
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true

        // Enable pinch to zoom without the zoom buttons
        settings.builtInZoomControls = true

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            // Hide the zoom controls for HONEYCOMB+
            settings.displayZoomControls = false
        }

        // Enable remote debugging via chrome://inspect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        // We set the WebViewClient to ensure links are consumed by the WebView rather
        // than passed to a browser if it can
        this.webView.setWebViewClient(WebViewClient())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }

        var results: Array<Uri>? = null

        // Check that the response is a good one
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                // If there is not data, then we may have taken a photo
                if (mCameraPhotoPath != null) {
                    results = arrayOf(Uri.parse(mCameraPhotoPath))
                }
            } else {
                val dataString = data.dataString
                if (dataString != null) {
                    results = arrayOf(Uri.parse(dataString))
                }
            }
        }

        mFilePathCallback!!.onReceiveValue(results)
        mFilePathCallback = null
        return
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }
    }

    private fun initFirebase() {
        myRef = FirebaseDatabase.getInstance().getReference()
    }

//    init {
//        val webSettings = settings
//        webSettings.javaScriptEnabled = true
//        webSettings.domStorageEnabled = true
//
//        webSettings.loadWithOverviewMode = true
//        webSettings.useWideViewPort = true
//        webSettings.builtInZoomControls = true
//        webSettings.displayZoomControls = false
//        webSettings.setSupportZoom(true)
//        webSettings.defaultTextEncodingName = "utf-8"
//        webSettings.allowFileAccess = true
//        settings.pluginState = WebSettings.PluginState.ON
//        webSettings.javaScriptCanOpenWindowsAutomatically = true
//
//        webViewClient = WebViewClient()
//        //webChromeClient = WebChromClient(context, SplashActivity.mCameraPhotoPath)
//        layoutParams = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
//        )
//
//        if(Build.VERSION.SDK_INT >= 21){
//            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
//        } else{
//            CookieManager.getInstance().setAcceptCookie(true);
//        }
//    }
}
