package com.xda.nachonotch

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.view.Display
import android.view.IRotationWatcher
import com.xda.nachonotch.util.cachedRotation
import com.xda.nachonotch.util.refreshScreenSize
import com.xda.nachonotch.util.rotation
import java.lang.reflect.Method

@SuppressLint("PrivateApi")
class App : Application() {
    private val iWindowManagerClass: Class<*> = Class.forName("android.view.IWindowManager")
    private val iWindowManager: Any by lazy {
        run {
            val stubClass = Class.forName("android.view.IWindowManager\$Stub")
            val serviceManagerClass = Class.forName("android.os.ServiceManager")

            val binder = serviceManagerClass.getMethod("checkService", String::class.java).invoke(null, Context.WINDOW_SERVICE)
            stubClass.getMethod("asInterface", IBinder::class.java).invoke(null, binder)
        }
    }
    private val rotationWatcher by lazy { RotationWatcher() }

    val rotationWatchers = ArrayList<IRotationWatcher>()

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val forName = Class::class.java.getDeclaredMethod("forName", String::class.java)
            val getDeclaredMethod = Class::class.java.getDeclaredMethod("getDeclaredMethod", String::class.java, arrayOf<Class<*>>()::class.java)

            val vmRuntimeClass = forName.invoke(null, "dalvik.system.VMRuntime") as Class<*>
            val getRuntime = getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null) as Method
            val setHiddenApiExemptions = getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", arrayOf(arrayOf<String>()::class.java)) as Method

            val vmRuntime = getRuntime.invoke(null)

            setHiddenApiExemptions.invoke(vmRuntime, arrayOf("L"))
        }

        refreshScreenSize()
        cachedRotation = rotation

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
            iWindowManagerClass
                    .run {
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
            cachedRotation = rotation
            refreshScreenSize()

            rotationWatchers.forEach { it.onRotationChanged(rotation) }
        }
    }
}