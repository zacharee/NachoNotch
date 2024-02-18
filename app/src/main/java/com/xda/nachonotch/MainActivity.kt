package com.xda.nachonotch

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.xda.nachonotch.activities.BaseActivity
import com.xda.nachonotch.util.PrefManager
import com.xda.nachonotch.util.addOverlayAndEnable
import com.xda.nachonotch.util.enforceTerms
import com.xda.nachonotch.util.launchOverlaySettings
import com.xda.nachonotch.util.prefManager
import com.xda.nachonotch.util.removeOverlayAndDisable

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!enforceTerms()) finish()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkCallingOrSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }
    }

    @Composable
    override fun Content() {
        AndroidView(factory = { layoutInflater.inflate(R.layout.activity_main, null) })
    }

    class Prefs : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
        private val switch by lazy { findPreference<SwitchPreference>("enabled_indicator") }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.prefs_home, rootKey)

            requireContext().apply {
                switch?.isChecked = prefManager.isEnabled
                switch?.setOnPreferenceClickListener {
                    if (Settings.canDrawOverlays(this)) {
                        if (prefManager.isEnabled) removeOverlayAndDisable()
                        else addOverlayAndEnable()
                        true
                    } else {
                        launchOverlaySettings()
                        false
                    }
                }
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            requireContext().prefManager.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onDestroy() {
            super.onDestroy()

            requireContext().prefManager.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            when (key) {
                PrefManager.SHOULD_RUN -> {
                    switch?.isChecked = requireContext().prefManager.isEnabled
                }
            }
        }
    }
}