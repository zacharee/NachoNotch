package com.xda.nachonotch.services

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.IBinder
import android.preference.PreferenceManager
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat
import com.xda.nachonotch.R
import com.xda.nachonotch.util.enforceTerms
import com.xda.nachonotch.util.launchOverlaySettings
import com.xda.nachonotch.util.prefManager

class ToggleService : TileService() {
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, ser: IBinder?) {
            prefManager.isEnabled = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            prefManager.isEnabled = false
        }
    }

    override fun onCreate() {
        bindService(Intent(this, BackgroundHandler::class.java), connection, 0)
    }

    override fun onStartListening() {
        qsTile?.state = if (prefManager.isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile?.label = resources.getString(if (prefManager.isEnabled) R.string.show_notch else R.string.hide_notch)
        qsTile?.updateTile()
    }

    override fun onClick() {
        if (enforceTerms()) {
            if (Settings.canDrawOverlays(this)) {
                if (prefManager.isEnabled) removeOverlayAndDisable()
                else addOverlayAndEnable()
            } else {
                launchOverlaySettings()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(connection)
    }

    private fun addOverlayAndEnable() {
        if (Settings.canDrawOverlays(this)) {
            prefManager.isEnabled = true

            val service = Intent(this, BackgroundHandler::class.java)
            ContextCompat.startForegroundService(this, service)

            qsTile?.state = Tile.STATE_ACTIVE
            qsTile?.label = resources.getString(R.string.show_notch)
            qsTile?.updateTile()
        } else {
            launchOverlaySettings()
        }
    }

    private fun removeOverlayAndDisable() {
        qsTile?.state = Tile.STATE_INACTIVE
        qsTile?.label = resources.getString(R.string.hide_notch)
        qsTile?.updateTile()

        val service = Intent(this, BackgroundHandler::class.java)
        stopService(service)

        prefManager.isEnabled = false
    }
}
