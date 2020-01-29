package com.grishberg.yayp

import android.app.Application
import com.grishberg.yayp.BuildConfig.METRICA_API_KEY
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig


class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Creating an extended library configuration.
        // Creating an extended library configuration.
        val config: YandexMetricaConfig =
            YandexMetricaConfig.newConfigBuilder(METRICA_API_KEY).build()
        // Initializing the AppMetrica SDK.
        // Initializing the AppMetrica SDK.
        YandexMetrica.activate(applicationContext, config)
        // Automatic tracking of user activity.
        // Automatic tracking of user activity.
        YandexMetrica.enableActivityAutoTracking(this)
    }
}