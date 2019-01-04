package com.xda.nachonotch.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.xda.nachonotch.R
import com.xda.nachonotch.util.Utils
import tk.zwander.seekbarpreference.SeekBarPreference

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Utils.enforceTerms(this)) finish()
        else {
            setContentView(R.layout.activity_settings)

            supportFragmentManager?.beginTransaction()?.replace(R.id.content, MainFragment())?.commit()
        }
    }

    class MainFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.prefs_main, rootKey)

            setListeners()
        }

        private fun setListeners() {
            val statusHeight = findPreference("status_height") as SeekBarPreference
            val navHeight = findPreference("nav_height") as SeekBarPreference

            preferenceManager.sharedPreferences.apply {
                if (!contains("status_height")) statusHeight.progress = Utils.getResourceStatusHeight(activity!!)
                if (!contains("nav_height")) navHeight.progress = Utils.getResourceNavHeight(activity!!)
            }

            statusHeight.setDefaultValue(Utils.getResourceStatusHeight(activity!!))
            navHeight.setDefaultValue(Utils.getResourceNavHeight(activity!!))
        }
    }
}
