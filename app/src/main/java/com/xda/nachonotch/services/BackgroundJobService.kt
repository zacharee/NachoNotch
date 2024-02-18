package com.xda.nachonotch.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import androidx.core.content.ContextCompat
import com.bugsnag.android.Bugsnag

class BackgroundJobService : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        Bugsnag.leaveBreadcrumb("Running service start job.")
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