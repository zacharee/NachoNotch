package com.xda.nachonotch.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import com.xda.nachonotch.R
import com.xda.nachonotch.util.prefManager
import com.xda.nachonotch.util.resourceNavBarHeight

class BottomLeftCorner : BaseOverlay {
    override val backgroundResource = R.drawable.corner_left

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    init {
        scaleY = -1f
    }

    override fun onSetupParams() {
        with(params) {
            gravity = Gravity.BOTTOM or Gravity.LEFT
            width = context.prefManager.cornerWidthBottomPx
            height = context.prefManager.cornerHeightBottomPx
            y = context.prefManager.navBarHeight - context.resourceNavBarHeight
        }
    }
}