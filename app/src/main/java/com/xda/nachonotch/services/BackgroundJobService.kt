package com.xda.nachonotch.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import androidx.core.content.ContextCompat
import com.xda.nachonotch.util.LoggingBugsnag
import com.xda.nachonotch.util.prefManager

class BackgroundJobService : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        if (prefManager.isEnabled) {
            LoggingBugsnag.leaveBreadcrumb("Running service start job.")
            ContextCompat.startForegroundService(
                this,
                Intent(this, BackgroundHandler::class.java).apply {
                    putExtra(BackgroundHandler.EXTRA_WAS_SCHEDULED, true)
                },
            )
        } else {
            LoggingBugsnag.leaveBreadcrumb("Not running service start job since not enabled.")
        }

        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }
}