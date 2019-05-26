package com.xda.nachonotch.views

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND
import android.view.WindowManager.LayoutParams.MATCH_PARENT
import com.xda.nachonotch.util.prefManager

class TopOverlay(context: Context) : BaseOverlay(context, backgroundColor = Color.BLACK) {
    override val params: WindowManager.LayoutParams
        get() = super.params.apply {
            flags = flags or FLAG_DIM_BEHIND
            dimAmount = 0f
            gravity = Gravity.TOP
            width = MATCH_PARENT
            height = context.prefManager.statusBarHeight
        }
}