package com.xda.nachonotch

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.xda.nachonotch.util.LoggingBugsnag
import com.xda.nachonotch.util.PrefManager
import com.xda.nachonotch.util.addOverlayAndEnable
import com.xda.nachonotch.util.cachedRotation
import com.xda.nachonotch.util.launchOverlaySettings
import com.xda.nachonotch.util.prefManager
import com.xda.nachonotch.util.refreshScreenSize
import com.xda.nachonotch.util.removeOverlayAndDisable
import com.xda.nachonotch.util.rotation
import com.xda.nachonotch.util.updateServiceState
import org.lsposed.hiddenapibypass.HiddenApiBypass

@SuppressLint("PrivateApi")
class App : Application(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val iWindowManagerClass: Class<*> = Class.forName("android.view.IWindowManager")
    private val iWindowManager by lazy {
        val stubClass = Class.forName("android.view.IWindowManager\$Stub")
        val serviceManagerClass = Class.forName("android.os.ServiceManager")

        val binder = serviceManagerClass.getMethod("checkService", String::class.java)
            .invoke(null, Context.WINDOW_SERVICE)
        stubClass.getMethod("asInterface", IBinder::class.java).invoke(null, binder)
    }
    private val rotationWatcher by lazy { RotationWatcher() }

    val rotationWatchers = object : ArrayList<IRotationWatcher>() {
        override fun add(element: IRotationWatcher): Boolean {
            element.onRotationChanged(cachedRotation)
            return super.add(element)
        }
    }

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
        rotationWatcher.onRotationChanged(cachedRotation)
        prefManager.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PrefManager.SHOULD_RUN -> {
                if (Settings.canDrawOverlays(this)) {
                    if (!prefManager.isEnabled) removeOverlayAndDisable()
                    else addOverlayAndEnable()
                } else if (prefManager.isEnabled) {
                    launchOverlaySettings()
                    prefManager.isEnabled = false
                }
            }
        }
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

    inner class RotationWatcher : IRotationWatcher.Stub() {
        override fun onRotationChanged(rotation: Int) {
            LoggingBugsnag.leaveBreadcrumb("Rotation has changed to $rotation.")
            cachedRotation = rotation
            refreshScreenSize()

            rotationWatchers.forEach { it.onRotationChanged(rotation) }
        }
    }
}