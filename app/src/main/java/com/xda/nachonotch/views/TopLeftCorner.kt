package com.xda.nachonotch.views

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import com.xda.nachonotch.R
import com.xda.nachonotch.util.EnvironmentManager
import com.xda.nachonotch.util.PrefManager
import com.xda.nachonotch.util.environmentManager
import com.xda.nachonotch.util.prefManager

class TopLeftCorner(context: Context) : BaseOverlay(context, R.drawable.corner_left) {
    @SuppressLint("RtlHardcoded")
    override val params: WindowManager.LayoutParams = super.params.apply {
        gravity = Gravity.TOP or Gravity.LEFT
        width = context.prefManager.cornerWidthTopPx
        height = context.prefManager.cornerHeightTopPx
        y = context.prefManager.statusBarHeight
    }

    override val listenKeys: List<String>
        get() = listOf(PrefManager.STATUS_HEIGHT, PrefManager.TOP_CORNER_HEIGHT, PrefManager.TOP_CORNER_WIDTH)

    override fun onUpdateParams() {
        params.width = context.prefManager.cornerWidthTopPx
        params.height = context.prefManager.cornerHeightTopPx
        params.y = context.prefManager.statusBarHeight
    }

    override fun canAdd(): Boolean {
        return context.prefManager.useTopCorners
    }

    override fun canShow(): Boolean {
        return !context.environmentManager.environmentStatus.contains(EnvironmentManager.EnvironmentStatus.STATUS_IMMERSIVE)
                && checkLandscape()
    }
}