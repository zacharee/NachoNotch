package com.xda.nachonotch.views

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.display.DisplayManagerGlobal
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.*
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