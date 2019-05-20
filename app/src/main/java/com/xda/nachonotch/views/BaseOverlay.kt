package com.xda.nachonotch.views

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.WindowManager

abstract class BaseOverlay(context: Context, backgroundResource: Int = 0, backgroundColor: Int = Int.MIN_VALUE) : View(context) {
    var isAdded = false
        set(value) {
            field = value
            isWaitingToAdd = false
        }
    var isWaitingToAdd = false

    val params = WindowManager.LayoutParams().apply {
        flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

        type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_PRIORITY_PHONE
        } else {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }

        format = PixelFormat.TRANSLUCENT
    }

    init {
        if (backgroundResource != 0) {
            setBackgroundResource(backgroundResource)
        } else if (backgroundColor != Int.MIN_VALUE) {
            setBackgroundColor(backgroundColor)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAdded = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isAdded = false
    }

    final override fun setBackgroundResource(resid: Int) {
        super.setBackgroundResource(resid)
    }

    final override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(color)
    }
}