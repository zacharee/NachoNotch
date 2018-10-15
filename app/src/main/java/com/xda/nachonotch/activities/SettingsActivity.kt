package com.xda.nachonotch.activities

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.pavelsikun.seekbarpreference.SeekBarPreference
import com.xda.nachonotch.R
import com.xda.nachonotch.util.Utils

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Utils.enforceTerms(this)) finish()
        else {
            setContentView(R.layout.activity_settings)

            fragmentManager?.beginTransaction()?.replace(R.id.content, MainFragment())?.commit()
        }
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

            val statusHeight = findPreference("status_height") as SeekBarPreference
            val navHeight = findPreference("nav_height") as SeekBarPreference

            preferenceManager.sharedPreferences.apply {
                if (!contains("status_height")) statusHeight.currentValue = Utils.getResurceStatusHeight(activity)
                if (!contains("nav_height")) navHeight.currentValue = Utils.getResourceNavHeight(activity)
            }

            statusHeight.setDefaultValue(Utils.getResurceStatusHeight(activity))
            navHeight.setDefaultValue(Utils.getResourceNavHeight(activity))
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
