package com.xda.nachonotch.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.xda.nachonotch.R
import com.xda.nachonotch.util.PrefManager
import com.xda.nachonotch.util.enforceTerms
import com.xda.nachonotch.util.resourceNavBarHeight
import com.xda.nachonotch.util.resourceStatusBarHeight
import tk.zwander.seekbarpreference.SeekBarPreference

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!enforceTerms()) finish()
        else {
            setContentView(R.layout.activity_settings)

            supportFragmentManager.beginTransaction().replace(R.id.content, MainFragment()).commit()
        }
    }

    class MainFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.prefs_main, rootKey)

            setListeners()
        }

        private fun setListeners() {
            val statusHeight = findPreference<SeekBarPreference>(PrefManager.STATUS_HEIGHT) as SeekBarPreference
            val navHeight = findPreference<SeekBarPreference>(PrefManager.NAV_HEIGHT) as SeekBarPreference

            preferenceManager.sharedPreferences.apply {
                if (!contains(PrefManager.STATUS_HEIGHT)) statusHeight.progress = activity!!.resourceStatusBarHeight
                if (!contains(PrefManager.NAV_HEIGHT)) navHeight.progress = activity!!.resourceNavBarHeight
            }

            statusHeight.setDefaultValue(activity!!.resourceStatusBarHeight)
            navHeight.setDefaultValue(activity!!.resourceNavBarHeight)
        }
    }
}
