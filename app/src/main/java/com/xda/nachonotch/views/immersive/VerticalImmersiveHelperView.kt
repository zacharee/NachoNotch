package com.xda.nachonotch.views.immersive

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("ViewConstructor")
class VerticalImmersiveHelperView(
    context: Context,
    immersiveListener: (nav: Boolean, status: Boolean) -> Unit,
    layoutListener: (left: Int, top: Int, right: Int, bottom: Int) -> Unit,
) : BaseImmersiveHelperView(
    context,
    immersiveListener,
    layoutListener,
) {
    override fun updateDimensions() {
        params.width = 1
    }
}