package com.xda.nachonotch.activities

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.xda.nachonotch.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        fragmentManager?.beginTransaction()?.replace(R.id.content, MainFragment())?.commit()
    }

    class MainFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            addPreferencesFromResource(R.xml.prefs_main)

            setListeners()
        }

        private fun setListeners() {
            findPreference("enable_launcher").setOnPreferenceClickListener {
                activateLauncher()
                true
            }
        }

        private fun activateLauncher() {
            val pm = context.packageManager
            val component = ComponentName(context.packageName, "${context.packageName}.MainActivity")

            if (pm.getComponentEnabledSetting(component) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                pm.setComponentEnabledSetting(
                        component,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                )
                Toast.makeText(context, resources.getText(R.string.activity_enabled), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
