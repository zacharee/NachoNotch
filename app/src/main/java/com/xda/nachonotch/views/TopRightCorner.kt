package com.xda.nachonotch.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.WindowManager
import com.xda.nachonotch.R
import com.xda.nachonotch.util.prefManager

class TopRightCorner(context: Context) : BaseOverlay(context, R.drawable.corner_left) {
    override val params: WindowManager.LayoutParams
        get() = super.params.apply {
            gravity = Gravity.TOP or Gravity.RIGHT
            width = context.prefManager.cornerWidthTopPx
            height = context.prefManager.cornerHeightTopPx
            y = context.prefManager.statusBarHeight
        }

    init {
        scaleX = -1f
    }
}