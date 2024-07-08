package com.xda.nachonotch.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import androidx.core.content.ContextCompat
import com.xda.nachonotch.util.LoggingBugsnag

class BackgroundJobService : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        LoggingBugsnag.leaveBreadcrumb("Running service start job.")
        ContextCompat.startForegroundService(
            this,
            Intent(this, BackgroundHandler::class.java),
        )

        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }
}