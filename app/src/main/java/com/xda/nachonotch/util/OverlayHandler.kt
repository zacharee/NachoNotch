package com.xda.nachonotch.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import android.view.DisplayInfo
import android.view.IRotationWatcher
import android.view.Surface
import com.xda.nachonotch.views.BaseOverlay
import com.xda.nachonotch.views.BottomLeftCorner
import com.xda.nachonotch.views.BottomOverlay
import com.xda.nachonotch.views.BottomRightCorner
import com.xda.nachonotch.views.TopLeftCorner
import com.xda.nachonotch.views.TopOverlay
import com.xda.nachonotch.views.TopRightCorner
import kotlinx.atomicfu.atomic

val Context.overlayHandler: OverlayHandler
    get() = OverlayHandler.getInstance(this)

class OverlayHandler private constructor(private val context: Context) : ImmersiveChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: OverlayHandler? = null

        @Synchronized
        fun getInstance(context: Context): OverlayHandler {
            return instance ?: OverlayHandler(context.applicationContext ?: context).also {
                instance = it
            }
        }
    }

    enum class OverlayPosition {
        TOP_BAR,
        BOTTOM_BAR,
        TOP_LEFT_CORNER,
        TOP_RIGHT_CORNER,
        BOTTOM_LEFT_CORNER,
        BOTTOM_RIGHT_CORNER
    }

    private val overlays = HashMap<OverlayPosition, BaseOverlay>()

    private val immersiveManager by lazy {
        ImmersiveHelperManager(context, this)
    }
    private val rotationWatcher by lazy {
        object : IRotationWatcher.Stub() {
            override fun onRotationChanged(rotation: Int) {
                immersiveManager.add()

                val displayInfo = DisplayInfo()
                context.displayCompat.getDisplayInfo(displayInfo)

                val hideBars = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val topCutoutInset = displayInfo.displayCutout?.safeInsets?.top
                    LoggingBugsnag.leaveBreadcrumb("Top cutout inset $topCutoutInset")

                    (topCutoutInset?.let { it <= 0 }) ?: (rotation != Surface.ROTATION_0)
                } else {
                    rotation != Surface.ROTATION_0
                }

                if (!hideBars) {
                    context.environmentManager.removeStatus(EnvironmentManager.EnvironmentStatus.LANDSCAPE)
                } else {
                    context.environmentManager.addStatus(EnvironmentManager.EnvironmentStatus.LANDSCAPE)
                }
            }
        }
    }
    private val created = atomic(false)

    init {
        overlays[OverlayPosition.TOP_BAR] = TopOverlay(context)
        overlays[OverlayPosition.BOTTOM_BAR] = BottomOverlay(context)

        overlays[OverlayPosition.TOP_LEFT_CORNER] = TopLeftCorner(context)
        overlays[OverlayPosition.TOP_RIGHT_CORNER] = TopRightCorner(context)
        overlays[OverlayPosition.BOTTOM_LEFT_CORNER] = BottomLeftCorner(context)
        overlays[OverlayPosition.BOTTOM_RIGHT_CORNER] = BottomRightCorner(context)
    }

    fun onCreate() {
        LoggingBugsnag.leaveBreadcrumb("Calling OverlayHandler#onCreate()")

        if (!created.value) {
            LoggingBugsnag.leaveBreadcrumb("Creating overlay stuff")

            immersiveManager.onCreate()
            context.prefManager.registerOnSharedPreferenceChangeListener(this)
            context.app.rotationWatchers.add(rotationWatcher)
            overlays.values.forEach { it.onCreate() }
            created.value = true
        }
    }

    fun onDestroy() {
        LoggingBugsnag.leaveBreadcrumb("Calling OverlayHandler#onDestroy()")

        if (created.value) {
            LoggingBugsnag.leaveBreadcrumb("Destroying overlay stuff")

            removeAllOverlays()
            immersiveManager.onDestroy()
            context.app.rotationWatchers.remove(rotationWatcher)
            overlays.values.forEach { it.onDestroy() }
            context.prefManager.unregisterOnSharedPreferenceChangeListener(this)
            created.value = false
        }
    }

    override fun onImmersiveChange() {
        val status = immersiveManager.isStatusImmersive()

        if (status) {
            context.environmentManager.addStatus(EnvironmentManager.EnvironmentStatus.STATUS_IMMERSIVE)
        } else {
            context.environmentManager.removeStatus(EnvironmentManager.EnvironmentStatus.STATUS_IMMERSIVE)
        }

        immersiveManager.isNavImmersive { nav ->
            if (nav) {
                context.environmentManager.addStatus(EnvironmentManager.EnvironmentStatus.NAV_IMMERSIVE)
            } else {
                context.environmentManager.removeStatus(EnvironmentManager.EnvironmentStatus.NAV_IMMERSIVE)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (context.prefManager.isEnabled) {
            when (key) {
                PrefManager.ROUNDED_CORNERS_TOP -> {
                    removeOverlays(OverlayPosition.TOP_LEFT_CORNER, OverlayPosition.TOP_RIGHT_CORNER) {
                        addOverlays(OverlayPosition.TOP_LEFT_CORNER, OverlayPosition.TOP_RIGHT_CORNER)
                    }
                }
                PrefManager.ROUNDED_CORNERS_BOTTOM -> {
                    removeOverlays(OverlayPosition.BOTTOM_LEFT_CORNER, OverlayPosition.BOTTOM_RIGHT_CORNER) {
                        addOverlays(OverlayPosition.BOTTOM_LEFT_CORNER, OverlayPosition.BOTTOM_RIGHT_CORNER)
                    }
                }
                PrefManager.COVER_NAV -> {
                    removeOverlays(OverlayPosition.BOTTOM_BAR) {
                        addOverlays(OverlayPosition.BOTTOM_BAR)
                    }
                }
            }
        }
    }

    fun addOverlayAndEnable() {
        LoggingBugsnag.leaveBreadcrumb("Adding overlays.")

        immersiveManager.add()
        addAllOverlays()
    }

    private fun addAllOverlays() {
        if (Settings.canDrawOverlays(context)) {
            removeAllOverlays {
                if (!context.environmentManager.hasAllStatuses(EnvironmentManager.EnvironmentStatus.LANDSCAPE)) {
                    addOverlays(*overlays.keys.toTypedArray())
                }
            }
        } else {
            context.launchOverlaySettings()
        }
    }

    fun removeAllOverlays(finishedListener: (() -> Unit)? = null) {
        removeOverlays(*overlays.keys.toTypedArray(), finishedListener = finishedListener)
    }

    private fun addOverlays(vararg overlays: OverlayPosition) {
        overlays.forEach {
            this.overlays[it]?.add()
        }
    }

    private fun removeOverlays(vararg overlays: OverlayPosition, finishedListener: (() -> Unit)? = null) {
        var removed = 0

        overlays.forEach {
            this.overlays[it]?.remove {
                removed++

                if (removed == overlays.size) {
                    finishedListener?.invoke()
                }
            }
        }
    }
}
