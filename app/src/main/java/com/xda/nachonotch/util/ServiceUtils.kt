package com.xda.nachonotch.util

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.joaomgcd.taskerpluginlibrary.extensions.requestQuery
import com.xda.nachonotch.activities.tasker.TaskerDisableStateConfigureActivity
import com.xda.nachonotch.activities.tasker.TaskerEnableStateConfigureActivity
import com.xda.nachonotch.services.BackgroundHandler
import com.xda.nachonotch.services.BackgroundJobService

fun Context.updateServiceState(toggle: Boolean = false): Pair<Boolean, String?> {
    try {
        LoggingBugsnag.leaveBreadcrumb("Updating service state.")

        if (enforceTerms()) {
            if (toggle) {
                prefManager.isEnabled = !prefManager.isEnabled
            }

            LoggingBugsnag.leaveBreadcrumb("Terms agreed to, checking for state.")
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
                    return false to "No permission to show overlay."
                }
            } else {
                LoggingBugsnag.leaveBreadcrumb("Not enabled, stopping service.")
                stopService(Intent(this, BackgroundHandler::class.java))
            }
        } else {
            LoggingBugsnag.leaveBreadcrumb("Terms not agreed to, terms Activity launched.")
            return false to "Terms not agreed to."
        }

        return true to null
    } finally {
        TaskerEnableStateConfigureActivity::class.java.requestQuery(this)
        TaskerDisableStateConfigureActivity::class.java.requestQuery(this)
    }
}

fun Context.scheduleService(): Boolean {
    LoggingBugsnag.leaveBreadcrumb("Scheduling service start.")
    val serviceComponent = ComponentName(this, BackgroundJobService::class.java)
    val builder = JobInfo.Builder(100, serviceComponent)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        builder.setExpedited(true)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        @Suppress("DEPRECATION")
        builder.setImportantWhileForeground(true)
        builder.setOverrideDeadline(5 * 1000)
    }

    val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler?

    return if (jobScheduler != null) {
        jobScheduler.schedule(builder.build()) == JobScheduler.RESULT_SUCCESS
    } else {
        false
    }
}
