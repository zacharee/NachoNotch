package com.xda.nachonotch.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.xda.nachonotch.R
import com.xda.nachonotch.activities.SettingsActivity
import com.xda.nachonotch.util.LoggingBugsnag
import com.xda.nachonotch.util.overlayHandler
import com.xda.nachonotch.util.prefManager
import com.xda.nachonotch.util.scheduleService

class BackgroundHandler : Service() {
    companion object {
        const val EXTRA_WAS_SCHEDULED = "was_scheduled"
    }

    override fun onCreate() {
        super.onCreate()

        LoggingBugsnag.leaveBreadcrumb("Service is creating.")

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            val notifMan = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notifMan.createNotificationChannel(NotificationChannel("nachonotch", resources.getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW))
        }

        overlayHandler.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LoggingBugsnag.leaveBreadcrumb("Received start command. Is enabled? ${prefManager.isEnabled}")

        if (prefManager.isEnabled) {
            LoggingBugsnag.leaveBreadcrumb("Attempting to start in foreground.")

            val notification = NotificationCompat.Builder(this, "nachonotch")
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText(resources.getString(R.string.settings_prompt))
                .setContentIntent(PendingIntent.getActivity(this, 100,
                    Intent(this, SettingsActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
                .setSmallIcon(R.drawable.qs_tile_icon)
                .setPriority(if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                    NotificationCompat.PRIORITY_MIN else NotificationCompat.PRIORITY_LOW)
                .build()

            try {
                LoggingBugsnag.leaveBreadcrumb("Attempting to start in foreground.")
                startForeground(1, notification)
                overlayHandler.addOverlayAndEnable()
            } catch (e: Throwable) {
                LoggingBugsnag.leaveBreadcrumb(
                    message = "Unable to start in foreground.",
                    error = e,
                )

                val wasScheduled = intent?.getBooleanExtra(EXTRA_WAS_SCHEDULED, false) == true

                stopSelf()

                if (wasScheduled) {
                    LoggingBugsnag.leaveBreadcrumb("Attempting to restart using scheduler.")
                    scheduleService()
                } else {
                    LoggingBugsnag.leaveBreadcrumb("Already started using scheduler, avoiding start loop and throwing.")
                    throw e
                }
            }
        } else stopSelf()

        return START_STICKY
    }

    override fun onDestroy() {
        LoggingBugsnag.leaveBreadcrumb("Service stopping.")
        super.onDestroy()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)

        overlayHandler.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }
}
