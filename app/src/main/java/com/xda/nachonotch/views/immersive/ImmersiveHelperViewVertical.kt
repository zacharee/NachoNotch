package com.xda.nachonotch.views.immersive

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.xda.nachonotch.util.ImmersiveHelperManager
import com.xda.nachonotch.util.resourceNavBarHeight
import com.xda.nachonotch.util.resourceStatusBarHeight
import kotlin.math.absoluteValue

@SuppressLint("ViewConstructor")
@Suppress("DEPRECATION")
class ImmersiveHelperViewVertical(context: Context, manager: ImmersiveHelperManager) : BaseImmersiveHelperView(context, manager) {
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val height = getProperScreenHeightForRotation().absoluteValue
        val statusHeight = context.resourceStatusBarHeight
        val navHeight = context.resourceNavBarHeight

        val full = h.absoluteValue >= height
        val status = h.absoluteValue + statusHeight >= height
        val nav = h.absoluteValue + navHeight >= height

        Log.e("NachoNotch", "h: $h, height: $height")

        manager.onFullChange(full)
        manager.onStatusChange(status || full)
        manager.onNavChange(nav || full)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val height = getProperScreenHeightForRotation().absoluteValue
        val statusHeight = context.resourceStatusBarHeight
        val navHeight = context.resourceNavBarHeight

        val h = top.absoluteValue + bottom.absoluteValue

        val full = h >= height
        val status = h + statusHeight >= height
        val nav = h + navHeight >= height

        Log.e("NachoNotch", "onLayout h: $h, height: $height, status: ${h + statusHeight}, nav: ${h + navHeight}")

        manager.onFullChange(full)
        manager.onStatusChange(status || full)
        manager.onNavChange(nav || full)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        manager.verticalHelperAdded = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        manager.verticalHelperAdded = false
    }
}