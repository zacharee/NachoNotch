package com.xda.nachonotch

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

class EnableActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pm = applicationContext.packageManager
        val component = ComponentName(applicationContext.packageName, "${applicationContext.packageName}.MainActivity")

        if (pm.getComponentEnabledSetting(componentName) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            pm.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
            )
            Toast.makeText(this, resources.getText(R.string.activity_enabled), Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}
