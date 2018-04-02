package com.xda.nachonotch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            if (Utils.isEnabled(context)) {
                val service = Intent(context, BackgroundHandler::class.java)
                startForegroundService(context, service)
            }
        }
    }
}
