package com.xda.nachonotch.views

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import com.xda.nachonotch.R
import com.xda.nachonotch.util.Utils

class LeftCorner : LinearLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        background = resources.getDrawable(R.drawable.corner_left, null)
    }

    fun getParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams().apply {
            dimAmount = 0.001f
            flags  = WindowManager.LayoutParams.FLAG_DIM_BEHIND or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
            type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_PHONE else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            gravity = Gravity.TOP or Gravity.LEFT
            y = Utils.getStatusBarHeight(context)
            width =   WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            format = PixelFormat.TRANSLUCENT
        }
    }
}