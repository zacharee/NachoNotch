package com.xda.nachonotch.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.WindowManager
import com.xda.nachonotch.R
import com.xda.nachonotch.util.prefManager
import com.xda.nachonotch.util.resourceNavBarHeight

class BottomLeftCorner(context: Context) : BaseOverlay(context, R.drawable.corner_left) {
    override val params: WindowManager.LayoutParams
        get() = super.params.apply {
            gravity = Gravity.BOTTOM or Gravity.LEFT
            width = context.prefManager.cornerWidthBottomPx
            height = context.prefManager.cornerHeightBottomPx
            y = context.prefManager.navBarHeight - context.resourceNavBarHeight
        }

    init {
        scaleY = -1f
    }

    override fun canAdd(): Boolean {
        return context.prefManager.useBottomCorners
    }

    override fun canShow(): Boolean {
        return !environmentStatus.contains(EnvironmentStatus.NAV_IMMERSIVE)
                && !environmentStatus.contains(EnvironmentStatus.LANDSCAPE)
    }
}