package com.xda.nachonotch.services

import android.content.SharedPreferences
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.core.service.quicksettings.PendingIntentActivityWrapper
import androidx.core.service.quicksettings.TileServiceCompat
import com.xda.nachonotch.R
import com.xda.nachonotch.util.*
import com.xda.nachonotch.util.Utils.TERMS_VERSION

class ToggleService : TileService(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onStartListening() {
        qsTile?.state = if (prefManager.isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile?.label = resources.getString(if (prefManager.isEnabled) R.string.show_notch else R.string.hide_notch)
        qsTile?.updateTile()

        prefManager.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStopListening() {
        prefManager.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PrefManager.SHOULD_RUN -> {
                updateTileState()
            }
        }
    }

    override fun onClick() {
        if (prefManager.termsVersion >= TERMS_VERSION) {
            if (Settings.canDrawOverlays(this)) {
                if (prefManager.isEnabled) rem()
                else add()
            } else {
                val launchIntent = PendingIntentActivityWrapper(this, 100, getOverlaySettingsIntent(), 0, false)

                try {
                    TileServiceCompat.startActivityAndCollapse(
                        this,
                        launchIntent,
                    )
                } catch (e: Exception) {
                    launchIntent.intent.data = null
                    TileServiceCompat.startActivityAndCollapse(
                        this,
                        launchIntent,
                    )
                }

                Toast.makeText(this, R.string.enable_overlay_permission, Toast.LENGTH_LONG).show()
            }
        } else {
            val launchIntent = getTermsIntent()

            TileServiceCompat.startActivityAndCollapse(
                this,
                PendingIntentActivityWrapper(this, 101, launchIntent, 0, false),
            )
        }
    }

    private fun add() {
        prefManager.isEnabled = true

        updateTileState()
    }

    private fun rem() {
        updateTileState()

        prefManager.isEnabled = false
    }

    private fun updateTileState() {
        if (prefManager.isEnabled) {
            qsTile?.state = Tile.STATE_ACTIVE
            qsTile?.label = resources.getString(R.string.show_notch)
        } else {
            qsTile?.state = Tile.STATE_INACTIVE
            qsTile?.label = resources.getString(R.string.hide_notch)
        }

        qsTile?.updateTile()
    }
}
