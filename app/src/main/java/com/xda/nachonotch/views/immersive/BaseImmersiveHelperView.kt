package com.xda.nachonotch.views.immersive

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.provider.Settings.Global.POLICY_CONTROL
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import com.xda.nachonotch.util.*
import kotlinx.coroutines.launch

@SuppressLint("ViewConstructor")
@Suppress("DEPRECATION")
open class BaseImmersiveHelperView(context: Context, val manager: ImmersiveHelperManager) : View(context) {
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

    var immersiveListener: ((left: Int, top: Int, right: Int, bottom: Int) -> Unit)? = null

    init {
        alpha = 0f
        fitsSystemWindows = true
    }

    private val rect = Rect()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        mainHandler.post {
            rect.apply { getBoundsOnScreen(this) }

            immersiveListener?.invoke(rect.left, rect.top, rect.right, rect.bottom)
        }

        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        updateDimensions()
    }

    open fun updateDimensions() {
        val width = 1
        val height = WindowManager.LayoutParams.MATCH_PARENT

        val landscape = isLandscape

        val newW = if (landscape) height else width
        val newH = if (landscape) width else height

        var changed = false

        if (params.width != newW) {
            params.width = newW

            changed = true
        }

        if (params.height != newH) {
            params.height = newH

            changed = true
        }

        if (changed) updateLayout()
    }

    fun updateLayout() {
        mainScope.launch {
            try {
                context.wm.updateViewLayout(this@BaseImmersiveHelperView, params)
            } catch (e: Exception) {}
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