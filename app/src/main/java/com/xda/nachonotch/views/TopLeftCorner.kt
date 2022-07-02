package com.xda.nachonotch.views

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import com.xda.nachonotch.R
import com.xda.nachonotch.util.prefManager

class TopLeftCorner(context: Context) : BaseOverlay(context, R.drawable.corner_left) {
    override val params: WindowManager.LayoutParams
        @SuppressLint("RtlHardcoded")
        get() = super.params.apply {
            gravity = Gravity.TOP or Gravity.LEFT
            width = context.prefManager.cornerWidthTopPx
            height = context.prefManager.cornerHeightTopPx
            y = context.prefManager.statusBarHeight
        }

    override fun canAdd(): Boolean {
        return context.prefManager.useTopCorners
    }

    override fun canShow(): Boolean {
        return !environmentStatus.contains(EnvironmentStatus.STATUS_IMMERSIVE)
                && checkLandscape()
    }
}