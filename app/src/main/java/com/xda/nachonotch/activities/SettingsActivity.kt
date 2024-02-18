package com.xda.nachonotch.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.preference.PreferenceFragmentCompat
import com.xda.nachonotch.R
import com.xda.nachonotch.util.PrefManager
import com.xda.nachonotch.util.enforceTerms
import com.xda.nachonotch.util.resourceNavBarHeight
import com.xda.nachonotch.util.resourceStatusBarHeight
import tk.zwander.seekbarpreference.SeekBarPreference

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!enforceTerms()) finish()
    }

    @Composable
    override fun Content() {
        AndroidView(factory = { layoutInflater.inflate(R.layout.activity_settings, null) }) {
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

            preferenceManager.sharedPreferences?.apply {
                if (!contains(PrefManager.STATUS_HEIGHT)) statusHeight.progress = requireActivity().resourceStatusBarHeight
                if (!contains(PrefManager.NAV_HEIGHT)) navHeight.progress = requireActivity().resourceNavBarHeight
            }

            statusHeight.setDefaultValue(requireActivity().resourceStatusBarHeight)
            navHeight.setDefaultValue(requireActivity().resourceNavBarHeight)
        }
    }
}
