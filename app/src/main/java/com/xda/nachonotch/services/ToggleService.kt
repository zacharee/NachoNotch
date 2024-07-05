package com.xda.nachonotch.services

import android.content.SharedPreferences
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.xda.nachonotch.R
import com.xda.nachonotch.util.*

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
        if (enforceTerms()) {
            if (Settings.canDrawOverlays(this)) {
                if (prefManager.isEnabled) rem()
                else add()
            } else {
                launchOverlaySettings()
            }
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
