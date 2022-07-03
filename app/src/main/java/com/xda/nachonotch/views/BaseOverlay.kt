package com.xda.nachonotch.views

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.View
import android.view.WindowManager
import java.util.concurrent.ConcurrentHashMap

abstract class BaseOverlay(
    context: Context,
    backgroundResource: Int = 0,
    backgroundColor: Int = Int.MIN_VALUE
) : View(context) {
    enum class EnvironmentStatus {
        STATUS_IMMERSIVE,
        NAV_IMMERSIVE,
        LANDSCAPE
    }

    open val params = WindowManager.LayoutParams().apply {
        @Suppress("DEPRECATION")
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }

        type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PRIORITY_PHONE
        } else {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }

        alpha = 1f
        format = PixelFormat.RGBA_8888
    }

    protected val environmentStatus: ConcurrentHashMap.KeySetView<EnvironmentStatus, Boolean> = ConcurrentHashMap.newKeySet()

    init {
        if (backgroundResource != 0) {
            setBackgroundResource(backgroundResource)
        } else if (backgroundColor != Int.MIN_VALUE) {
            setBackgroundColor(backgroundColor)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isForceDarkAllowed = false
        }
    }

    protected abstract fun canAdd(): Boolean
    protected abstract fun canShow(): Boolean

    open fun update(wm: WindowManager) {
        try {
            wm.updateViewLayout(this, params)
        } catch (_: Exception) { }
    }

    open fun add(wm: WindowManager) {
        if (canAdd()) {
            try {
                wm.addView(this, params)
            } catch (_: Exception) { }
        }
    }

    open fun remove(wm: WindowManager) {
        try {
            wm.removeView(this)
        } catch (_: Exception) { }
    }

    open fun show(wm: WindowManager) {
        params.alpha = 1f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        }
        update(wm)
    }

    open fun hide(wm: WindowManager) {
        params.alpha = 0f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }
        update(wm)
    }

    fun addStatus(windowManager: WindowManager, vararg status: EnvironmentStatus) {
        environmentStatus.addAll(status)
        onStatusUpdate(windowManager)
    }

    fun removeStatus(windowManager: WindowManager, vararg status: EnvironmentStatus) {
        environmentStatus.removeAll(status.toSet())
        onStatusUpdate(windowManager)
    }

    final override fun setBackgroundResource(resid: Int) {
        super.setBackgroundResource(resid)
    }

    final override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(color)
    }

    protected fun checkLandscape(): Boolean {
        return !environmentStatus.contains(EnvironmentStatus.LANDSCAPE)
    }

    private fun onStatusUpdate(windowManager: WindowManager) {
        if (canShow()) {
            show(windowManager)
        } else {
            hide(windowManager)
        }
    }
}