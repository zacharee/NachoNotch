package com.zacharee1.nachonotch

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class ToggleService : TileService() {
    val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, ser: IBinder?) {
            isEnabled = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isEnabled = false
        }
    }

    private var isEnabled = false

    override fun onCreate() {
        bindService(Intent(this, BackgroundHandler::class.java), connection, 0)
        isEnabled = BackgroundHandler.RUNNING
    }

    override fun onStartListening() {
        qsTile?.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile?.label = resources.getString(if (isEnabled) R.string.show_notch else R.string.hide_notch)
        qsTile?.updateTile()
    }

    override fun onClick() {
        if (Settings.canDrawOverlays(this)) {
            if (isEnabled) removeOverlayAndDisable() else addOverlayAndEnable()
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + applicationContext.packageName))
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(connection)
    }

    private fun addOverlayAndEnable() {
        val service = Intent(this, BackgroundHandler::class.java)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) startService(service)
        else startForegroundService(service)

        isEnabled = true
        qsTile?.state = Tile.STATE_ACTIVE
        qsTile?.label = resources.getString(R.string.show_notch)
        qsTile?.updateTile()
    }

    private fun removeOverlayAndDisable() {
        isEnabled = false
        qsTile?.state = Tile.STATE_INACTIVE
        qsTile?.label = resources.getString(R.string.hide_notch)
        qsTile?.updateTile()

        val service = Intent(this, BackgroundHandler::class.java)
        stopService(service)
    }
}
