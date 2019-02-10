package com.xda.nachonotch.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.WindowManager
import com.xda.nachonotch.util.prefManager
import com.xda.nachonotch.util.resourceNavBarHeight

class BottomOverlay : BaseOverlay {
    override val backgroundColor = Color.BLACK

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    override fun onSetupParams() {
        with(params) {
            flags = flags or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
            gravity = Gravity.BOTTOM
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = context.prefManager.navBarHeight
            y = -context.resourceNavBarHeight
        }
    }
}