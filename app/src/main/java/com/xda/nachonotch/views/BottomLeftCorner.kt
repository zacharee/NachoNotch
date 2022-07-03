package com.xda.nachonotch.views

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import com.xda.nachonotch.R
import com.xda.nachonotch.util.*

class BottomLeftCorner(context: Context) : BaseOverlay(context, R.drawable.corner_left) {
    override val params: WindowManager.LayoutParams
        @SuppressLint("RtlHardcoded")
        get() = super.params.apply {
            gravity = Gravity.BOTTOM or Gravity.LEFT
            width = context.prefManager.cornerWidthBottomPx
            height = context.prefManager.cornerHeightBottomPx
            y = context.prefManager.navBarHeight - context.resourceNavBarHeight
        }

    override val listenKeys: List<String>
        get() = listOf(PrefManager.NAV_HEIGHT, PrefManager.BOTTOM_CORNER_HEIGHT, PrefManager.BOTTOM_CORNER_WIDTH)

    init {
        scaleY = -1f
    }

    override fun onUpdateParams() {
        params.width = context.prefManager.cornerWidthBottomPx
        params.height = context.prefManager.cornerHeightBottomPx
        params.y = context.prefManager.navBarHeight - context.resourceNavBarHeight
    }

    override fun canAdd(): Boolean {
        return context.prefManager.useBottomCorners
    }

    override fun canShow(): Boolean {
        return !context.environmentManager.environmentStatus.contains(EnvironmentManager.EnvironmentStatus.NAV_IMMERSIVE)
                && checkLandscape()
    }
}