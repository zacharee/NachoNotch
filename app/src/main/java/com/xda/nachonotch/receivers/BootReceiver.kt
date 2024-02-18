package com.xda.nachonotch.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.xda.nachonotch.App


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            (context.applicationContext as App).updateServiceState()
        }
    }
}
