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
import android.support.v4.content.ContextCompat
import com.xda.nachonotch.R
import com.xda.nachonotch.util.Utils

class ToggleService : TileService() {
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, ser: IBinder?) {
            Utils.setEnabled(this@ToggleService, true)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Utils.setEnabled(this@ToggleService, false)
        }
    }
    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        bindService(Intent(this, BackgroundHandler::class.java), connection, 0)
    }

    override fun onStartListening() {
        qsTile?.state = if (Utils.isEnabled(this)) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile?.label = resources.getString(if (Utils.isEnabled(this)) R.string.show_notch else R.string.hide_notch)
        qsTile?.updateTile()
    }

    override fun onClick() {
        if (Settings.canDrawOverlays(this)) {
            if (Utils.isEnabled(this)) removeOverlayAndDisable() else addOverlayAndEnable()
        } else {
            Utils.launchOverlaySettings(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(connection)
    }

    private fun addOverlayAndEnable() {
        if (Settings.canDrawOverlays(this)) {
            Utils.setEnabled(this, true)

            val service = Intent(this, BackgroundHandler::class.java)
            ContextCompat.startForegroundService(this, service)

            qsTile?.state = Tile.STATE_ACTIVE
            qsTile?.label = resources.getString(R.string.show_notch)
            qsTile?.updateTile()
        } else {
            Utils.launchOverlaySettings(this)
        }
    }

    private fun removeOverlayAndDisable() {
        qsTile?.state = Tile.STATE_INACTIVE
        qsTile?.label = resources.getString(R.string.hide_notch)
        qsTile?.updateTile()

        val service = Intent(this, BackgroundHandler::class.java)
        stopService(service)

        Utils.setEnabled(this, false)
    }
}
