package com.xda.nachonotch.receivers

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PersistableBundle
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.xda.nachonotch.services.BackgroundHandler
import com.xda.nachonotch.services.BackgroundJobService
import com.xda.nachonotch.util.launchOverlaySettings
import com.xda.nachonotch.util.prefManager
import java.util.concurrent.TimeUnit


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            if (context.prefManager.isEnabled) {
                if (Settings.canDrawOverlays(context)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        scheduleService(context)
                    } else {
                        val service = Intent(context, BackgroundHandler::class.java)
                        ContextCompat.startForegroundService(context, service)
                    }
                } else {
                    context.launchOverlaySettings()
                }
            }
        }
    }

    private fun scheduleService(context: Context): Boolean {
        val serviceComponent = ComponentName(context, BackgroundJobService::class.java)
        val builder = JobInfo.Builder(100, serviceComponent)

        builder.setMinimumLatency(5 * 1000)
        builder.setOverrideDeadline(10 * 1000)

        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler?

        return if (jobScheduler != null) {
            jobScheduler.schedule(builder.build()) == JobScheduler.RESULT_SUCCESS
        } else {
            false
        }
    }
}
