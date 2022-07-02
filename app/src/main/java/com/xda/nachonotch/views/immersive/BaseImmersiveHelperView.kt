package com.xda.nachonotch.views.immersive

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.xda.nachonotch.util.*
import kotlinx.coroutines.launch

@SuppressLint("ViewConstructor")
@Suppress("DEPRECATION")
open class BaseImmersiveHelperView(context: Context, val manager: ImmersiveHelperManager,
                                   private val immersiveListener: (left: Int, top: Int, right: Int, bottom: Int) -> Unit) : View(context) {
    @SuppressLint("RtlHardcoded")
    val params = WindowManager.LayoutParams().apply {
        type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_PHONE
        else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        format = PixelFormat.TRANSPARENT
        x = 0
        y = 0
        gravity = Gravity.LEFT or Gravity.BOTTOM
    }

    init {
        alpha = 0f
        fitsSystemWindows = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isForceDarkAllowed = false
        }
    }

    private val rect = Rect()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        mainHandler.post {
            rect.apply { getBoundsOnScreen(this) }

            immersiveListener.invoke(rect.left, rect.top, rect.right, rect.bottom)
        }

        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        updateDimensions()
        updateLayout()
    }

    open fun updateDimensions() {}

    fun updateLayout() {
        mainScope.launch {
            try {
                context.wm.updateViewLayout(this@BaseImmersiveHelperView, params)
            } catch (_: Exception) {}
        }
    }

    fun enterNavImmersive() {
        mainScope.launch {
            systemUiVisibility = systemUiVisibility or
                    SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    fun exitNavImmersive() {
        mainScope.launch {
            systemUiVisibility = systemUiVisibility and
                    SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv() and
                    SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()
        }
    }
}