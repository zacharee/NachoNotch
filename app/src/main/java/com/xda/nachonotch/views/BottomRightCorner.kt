package com.xda.nachonotch.views

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import com.xda.nachonotch.R
import com.xda.nachonotch.util.prefManager
import com.xda.nachonotch.util.resourceNavBarHeight

class BottomRightCorner(context: Context) : BaseOverlay(context, R.drawable.corner_left) {
    override val params: WindowManager.LayoutParams
        @SuppressLint("RtlHardcoded")
        get() = super.params.apply {
            gravity = Gravity.BOTTOM or Gravity.RIGHT
            width = context.prefManager.cornerWidthBottomPx
            height = context.prefManager.cornerHeightBottomPx
            y = context.prefManager.navBarHeight - context.resourceNavBarHeight
        }

    init {
        scaleY = -1f
        scaleX = -1f
    }

    override fun canAdd(): Boolean {
        return context.prefManager.useBottomCorners
    }

    override fun canShow(): Boolean {
        return !environmentStatus.contains(EnvironmentStatus.NAV_IMMERSIVE)
                && checkLandscape()
    }
}