package com.xda.nachonotch.views.immersive

import android.annotation.SuppressLint
import android.content.Context
import com.xda.nachonotch.util.ImmersiveHelperManager

@SuppressLint("ViewConstructor")
class VerticalImmersiveHelperView(context: Context, manager: ImmersiveHelperManager,
                                  immersiveListener: (left: Int, top: Int, right: Int, bottom: Int) -> Unit) : BaseImmersiveHelperView(context, manager, immersiveListener) {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        manager.verticalHelperAdded = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        manager.verticalHelperAdded = false
    }

    override fun updateDimensions() {
        params.width = 1
    }
}