package com.xda.nachonotch.activities

import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity
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
            val statusHeight = findPreference("status_height") as SeekBarPreference
            val navHeight = findPreference("nav_height") as SeekBarPreference

            preferenceManager.sharedPreferences.apply {
                if (!contains("status_height")) statusHeight.currentValue = Utils.getResourceStatusHeight(activity)
                if (!contains("nav_height")) navHeight.currentValue = Utils.getResourceNavHeight(activity)
            }

            statusHeight.setDefaultValue(Utils.getResourceStatusHeight(activity))
            navHeight.setDefaultValue(Utils.getResourceNavHeight(activity))
        }
    }
}
