package com.xda.nachonotch.views.immersive

import android.annotation.SuppressLint
import android.content.Context
import android.view.WindowManager
import com.xda.nachonotch.util.ImmersiveHelperManager
import kotlin.math.absoluteValue

@SuppressLint("ViewConstructor")
@Suppress("DEPRECATION")
class ImmersiveHelperViewHorizontal(context: Context, manager: ImmersiveHelperManager) : BaseImmersiveHelperView(context, manager) {
    init {
        alpha = 0f
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        manager.horizontalHelperAdded = true

        WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        manager.horizontalHelperAdded = false
    }

    override fun updateDimensions() {
        val width = WindowManager.LayoutParams.MATCH_PARENT
        val height = 1

        var changed = false

        if (params.width != width) {
            params.width = width

            changed = true
        }

        if (params.height != height) {
            params.height = height

            changed = true
        }

        if (changed) updateLayout()
    }
}