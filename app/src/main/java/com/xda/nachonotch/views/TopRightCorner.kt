package com.xda.nachonotch.views

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.xda.nachonotch.R
import com.xda.nachonotch.util.prefManager

class TopRightCorner : View {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        background = resources.getDrawable(R.drawable.corner_left, null)
        scaleX = -1f
    }

    fun getParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams().apply {
            flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_PHONE
            else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            gravity = Gravity.TOP or Gravity.RIGHT
            width = context.prefManager.cornerWidthTopPx
            height = context.prefManager.cornerHeightTopPx
            format = PixelFormat.TRANSLUCENT
            y = context.prefManager.statusBarHeight
        }
    }
}