package com.xda.nachonotch.views

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.xda.nachonotch.R
import com.xda.nachonotch.util.Utils
import com.xda.nachonotch.util.prefManager
import com.xda.nachonotch.util.resourceNavBarHeight

class BottomRightCorner : View {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        background = resources.getDrawable(R.drawable.corner_left, null)
        scaleY = -1f
        scaleX = -1f
    }

    fun getParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams().apply {
            flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_PHONE
            else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            gravity = Gravity.BOTTOM or Gravity.RIGHT
            width =  context.prefManager.cornerWidthBottomPx
            height = context.prefManager.cornerHeightBottomPx
            format = PixelFormat.TRANSLUCENT
            y = context.prefManager.navBarHeight - context.resourceNavBarHeight
        }
    }
}