package com.xda.nachonotch

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import android.widget.Toast

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val component = ComponentName(this, MainActivity::class.java)

        if (packageManager.getComponentEnabledSetting(component) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            packageManager.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
            )
        }
    }
}