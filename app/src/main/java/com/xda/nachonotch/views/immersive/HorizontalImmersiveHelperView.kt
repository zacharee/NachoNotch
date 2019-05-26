package com.xda.nachonotch.views.immersive

import android.content.Context
import com.xda.nachonotch.util.ImmersiveHelperManager

class HorizontalImmersiveHelperView(context: Context, manager: ImmersiveHelperManager,
                                    immersiveListener: (left: Int, top: Int, right: Int, bottom: Int) -> Unit) : BaseImmersiveHelperView(context, manager, immersiveListener) {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        manager.horizontalHelperAdded = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        manager.horizontalHelperAdded = false
    }

    override fun updateDimensions() {
        params.height = 1
    }
}