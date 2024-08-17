package com.xda.nachonotch.util

import android.annotation.TargetApi
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.xda.nachonotch.services.BackgroundHandler
import com.xda.nachonotch.services.BackgroundJobService

fun Context.updateServiceState() {
    LoggingBugsnag.leaveBreadcrumb("Updating service state.")
    if (prefManager.isEnabled) {
        LoggingBugsnag.leaveBreadcrumb("Hide enabled.")
        if (Settings.canDrawOverlays(this)) {
            LoggingBugsnag.leaveBreadcrumb("Has permission to show, starting service.")
            try {
                val service = Intent(this, BackgroundHandler::class.java)
                ContextCompat.startForegroundService(this, service)
            } catch (e: Throwable) {
                LoggingBugsnag.leaveBreadcrumb(
                    message = "Unable to directly start foreground service.",
                    error = e,
                )
                scheduleService()
            }
        } else {
            LoggingBugsnag.leaveBreadcrumb("Has no permission, requesting.")
            launchOverlaySettings()
        }
    } else {
        LoggingBugsnag.leaveBreadcrumb("Not enabled, stopping service.")
        stopService(Intent(this, BackgroundHandler::class.java))
    }
}

@TargetApi(Build.VERSION_CODES.Q)
fun Context.scheduleService(): Boolean {
    LoggingBugsnag.leaveBreadcrumb("Scheduling service start.")
    val serviceComponent = ComponentName(this, BackgroundJobService::class.java)
    val builder = JobInfo.Builder(100, serviceComponent)

    builder.setMinimumLatency(0)
    builder.setOverrideDeadline(5 * 1000)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        builder.setExpedited(true)
    } else {
        @Suppress("DEPRECATION")
        builder.setImportantWhileForeground(true)
    }

    val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler?

    return if (jobScheduler != null) {
        jobScheduler.schedule(builder.build()) == JobScheduler.RESULT_SUCCESS
    } else {
        false
    }
}
