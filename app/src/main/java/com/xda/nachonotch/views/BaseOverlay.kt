package com.xda.nachonotch.views

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager

abstract class BaseOverlay : View {
    open val backgroundResource = 0
    open val backgroundColor = Integer.MIN_VALUE

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

        onSetupParams()
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (backgroundResource > 0) {
            background = resources.getDrawable(backgroundResource, context.theme)
        } else if (backgroundColor > Integer.MIN_VALUE) {
            setBackgroundColor(backgroundColor)
        }
    }

    open fun onSetupParams() {}
}