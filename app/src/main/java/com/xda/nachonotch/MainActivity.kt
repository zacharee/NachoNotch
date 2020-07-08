package com.xda.nachonotch

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.service.quicksettings.Tile
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.xda.nachonotch.activities.SettingsActivity
import com.xda.nachonotch.services.BackgroundHandler
import com.xda.nachonotch.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!enforceTerms()) finish()
        else {
            setContentView(R.layout.activity_main)
        }
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