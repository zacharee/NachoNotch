package com.xda.nachonotch.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import com.xda.nachonotch.R
import com.xda.nachonotch.util.prefManager

class TopLeftCorner(context: Context) : BaseOverlay(context, R.drawable.corner_left) {
    init {
        with(params) {
            gravity = Gravity.TOP or Gravity.LEFT
            width = context.prefManager.cornerWidthTopPx
            height = context.prefManager.cornerHeightTopPx
            y = context.prefManager.statusBarHeight
        }
    }
}