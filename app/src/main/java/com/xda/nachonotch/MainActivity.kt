package com.xda.nachonotch

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.xda.nachonotch.activities.BaseActivity
import com.xda.nachonotch.activities.SettingsActivity
import com.xda.nachonotch.util.*

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_terms -> {
                val termsIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/zacharee/NachoNotch/blob/master/app/src/main/assets/Terms.md"))
                startActivity(termsIntent)
                return true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }

        return super.onOptionsItemSelected(item)
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