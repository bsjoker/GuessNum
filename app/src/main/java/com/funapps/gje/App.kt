package com.funapps.gje

import android.app.Application
import com.onesignal.OneSignal
import com.truelove.kotlinplatinarush.network.iCountryApi
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // OneSignal Initialization
        OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init()

        instance = this
    }

    companion object{
        fun create(): iCountryApi {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://ip-api.com/")
                .build()

            return retrofit.create(iCountryApi::class.java)
        }

        lateinit var instance: App
            private set
    }
}