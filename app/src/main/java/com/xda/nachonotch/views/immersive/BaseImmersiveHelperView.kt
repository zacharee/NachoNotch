package com.xda.nachonotch.views.immersive

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.xda.nachonotch.util.ImmersiveHelperManager
import com.xda.nachonotch.util.mainHandler
import com.xda.nachonotch.util.realScreenSize
import com.xda.nachonotch.util.wm

@SuppressLint("ViewConstructor")
@Suppress("DEPRECATION")
open class BaseImmersiveHelperView(context: Context, val manager: ImmersiveHelperManager) : View(context) {
    val params = WindowManager.LayoutParams().apply {
        type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_PHONE
        else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        format = PixelFormat.TRANSPARENT
        x = 0
        y = 0
        gravity = Gravity.LEFT or Gravity.BOTTOM
    }

    init {
        alpha = 0f
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setBackgroundColor(Color.RED)
        updateDimensions()

        Log.e("NachoNotch", "attach")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        Log.e("NachoNotch", "detach")
    }

    open fun updateDimensions() {
        val width = 10
        val height = WindowManager.LayoutParams.MATCH_PARENT

        var changed = false

        if (params.width != width) {
            params.width = width

            changed = true
        }

        if (params.height != height) {
            params.height = height

            changed = true
        }

        if (changed) updateLayout()
    }

    fun updateLayout() {
        mainHandler.post {
            try {
                context.wm.updateViewLayout(this, params)
            } catch (e: Exception) {}
        }
    }

    fun isStatusImmersive(): Boolean {
        val imm = Settings.Global.getString(context.contentResolver, "policy_control")
        return imm?.contains("status") == true
                || isFullImmersive()
    }

    fun isNavImmersive(): Boolean {
        val imm = Settings.Global.getString(context.contentResolver, "policy_control")
        return imm?.contains("navigation") == true
                || isFullImmersive()
    }

    fun isFullImmersive(): Boolean {
        val imm = Settings.Global.getString(context.contentResolver, "policy_control")
        return imm?.contains("immersive.full") == true
                || systemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN != 0
    }

    fun getProperScreenHeightForRotation(): Int {
        return context.realScreenSize.y
    }
}