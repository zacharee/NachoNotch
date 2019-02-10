package com.xda.nachonotch.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.WindowManager
import com.xda.nachonotch.R
import com.xda.nachonotch.util.prefManager

class TopRightCorner : BaseOverlay {
    override val backgroundResource = R.drawable.corner_left

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    init {
        scaleX = -1f
    }

    override fun onSetupParams() {
        with(params) {
            flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            gravity = Gravity.TOP or Gravity.RIGHT
            width = context.prefManager.cornerWidthTopPx
            height = context.prefManager.cornerHeightTopPx
            y = context.prefManager.statusBarHeight
        }
    }
}