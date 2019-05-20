package com.xda.nachonotch.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import com.xda.nachonotch.R
import com.xda.nachonotch.util.prefManager
import com.xda.nachonotch.util.resourceNavBarHeight

class BottomLeftCorner(context: Context) : BaseOverlay(context, R.drawable.corner_left) {
    init {
        scaleY = -1f

        with(params) {
            gravity = Gravity.BOTTOM or Gravity.LEFT
            width = context.prefManager.cornerWidthBottomPx
            height = context.prefManager.cornerHeightBottomPx
            y = context.prefManager.navBarHeight - context.resourceNavBarHeight
        }
    }
}