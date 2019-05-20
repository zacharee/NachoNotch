package com.xda.nachonotch.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
import com.xda.nachonotch.util.prefManager

class TopOverlay(context: Context) : BaseOverlay(context, backgroundColor = Color.BLACK) {
    init {
        with(params) {
            flags = flags or FLAG_DIM_BEHIND or
                    FLAG_LAYOUT_INSET_DECOR
            dimAmount = 0.001f
            gravity = Gravity.TOP
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = context.prefManager.statusBarHeight
        }
    }
}