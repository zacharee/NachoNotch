package com.xda.nachonotch.views.immersive

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("ViewConstructor")
class HorizontalImmersiveHelperView(
    context: Context,
    immersiveListener: (nav: Boolean?, status: Boolean?) -> Unit,
    layoutListener: (left: Int, top: Int, right: Int, bottom: Int) -> Unit,
) : BaseImmersiveHelperView(
    context,
    immersiveListener,
    layoutListener,
    false,
) {
    override fun updateDimensions() {
        params.height = 1
    }
}