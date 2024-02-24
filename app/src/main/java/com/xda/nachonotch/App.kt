package com.xda.nachonotch

import android.annotation.SuppressLint
import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Display
import android.view.IRotationWatcher
import androidx.core.content.ContextCompat
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.performance.BugsnagPerformance
import com.xda.nachonotch.services.BackgroundHandler
import com.xda.nachonotch.services.BackgroundJobService
import com.xda.nachonotch.util.cachedRotation
import com.xda.nachonotch.util.launchOverlaySettings
import com.xda.nachonotch.util.prefManager
import com.xda.nachonotch.util.refreshScreenSize
import com.xda.nachonotch.util.rotation
import org.lsposed.hiddenapibypass.HiddenApiBypass

@SuppressLint("PrivateApi")
class App : Application() {
    private val iWindowManagerClass: Class<*> = Class.forName("android.view.IWindowManager")
    private val iWindowManager: Any by lazy {
        val stubClass = Class.forName("android.view.IWindowManager\$Stub")
        val serviceManagerClass = Class.forName("android.os.ServiceManager")

        val binder = serviceManagerClass.getMethod("checkService", String::class.java)
            .invoke(null, Context.WINDOW_SERVICE)
        stubClass.getMethod("asInterface", IBinder::class.java).invoke(null, binder)
    }
    private val rotationWatcher by lazy { RotationWatcher() }

    val rotationWatchers = ArrayList<IRotationWatcher>()

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.setHiddenApiExemptions("")
        }

        Bugsnag.start(this)
        BugsnagPerformance.start(this)

        refreshScreenSize()
        cachedRotation = rotation

        updateServiceState()

        val component = ComponentName(this, MainActivity::class.java)

        if (packageManager.getComponentEnabledSetting(component) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            packageManager.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }

        watchRotation(rotationWatcher, Display.DEFAULT_DISPLAY)
    }

    private fun watchRotation(watcher: IRotationWatcher, displayId: Int): Int {
        return try {
            iWindowManagerClass.run {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    getMethod("watchRotation", IRotationWatcher::class.java)
                        .invoke(iWindowManager, watcher) as Int
                } else {
                    getMethod("watchRotation", IRotationWatcher::class.java, Int::class.java)
                        .invoke(iWindowManager, watcher, displayId) as Int
                }
            }
        } catch (e: Exception) {
            0
        }
    }

    fun updateServiceState() {
        Bugsnag.leaveBreadcrumb("Updating service state.")
        if (prefManager.isEnabled) {
            Bugsnag.leaveBreadcrumb("Hide enabled.")
            if (Settings.canDrawOverlays(this)) {
                Bugsnag.leaveBreadcrumb("Has permission to show, starting service.")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    scheduleService()
                } else {
                    val service = Intent(this, BackgroundHandler::class.java)
                    ContextCompat.startForegroundService(this, service)
                }
            } else {
                Bugsnag.leaveBreadcrumb("Has no permission, requesting.")
                launchOverlaySettings()
            }
        } else {
            Bugsnag.leaveBreadcrumb("Not enabled, stopping service.")
            stopService(Intent(this, BackgroundHandler::class.java))
        }
    }

    private fun scheduleService(): Boolean {
        Bugsnag.leaveBreadcrumb("Scheduling service start.")
        val serviceComponent = ComponentName(this, BackgroundJobService::class.java)
        val builder = JobInfo.Builder(100, serviceComponent)

        builder.setMinimumLatency(0)
        builder.setOverrideDeadline(5 * 1000)

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler?

        return if (jobScheduler != null) {
            jobScheduler.schedule(builder.build()) == JobScheduler.RESULT_SUCCESS
        } else {
            false
        }
    }

    inner class RotationWatcher : IRotationWatcher.Stub() {
        override fun onRotationChanged(rotation: Int) {
            Bugsnag.leaveBreadcrumb("Rotation has changed to $rotation.")
            cachedRotation = rotation
            refreshScreenSize()

            rotationWatchers.forEach { it.onRotationChanged(rotation) }
        }
    }
}