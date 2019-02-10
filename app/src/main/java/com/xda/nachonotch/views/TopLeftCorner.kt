package com.xda.nachonotch.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import com.xda.nachonotch.R
import com.xda.nachonotch.util.prefManager

class TopLeftCorner : BaseOverlay {
    override val backgroundResource = R.drawable.corner_left

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    override fun onSetupParams() {
        with(params) {
            gravity = Gravity.TOP or Gravity.LEFT
            width = context.prefManager.cornerWidthTopPx
            height = context.prefManager.cornerHeightTopPx
            y = context.prefManager.statusBarHeight
        }
    }
}