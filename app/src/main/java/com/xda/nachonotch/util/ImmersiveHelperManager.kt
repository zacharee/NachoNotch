package com.xda.nachonotch.util

import android.content.Context
import com.xda.nachonotch.views.immersive.ImmersiveHelperViewVertical

class ImmersiveHelperManager(private val context: Context) : ImmersiveTypeListener {
    private val vertical = ImmersiveHelperViewVertical(context, this)

    private var statusImmersive = false
    private var navImmersive = false
    private var fullImmersive = false

    var verticalHelperAdded = false

    var changeListener: ImmersiveChangeListener? = null

    override fun onFullChange(isFull: Boolean) {
        fullImmersive = isFull
        changeListener?.onImmersiveChange()
    }

    override fun onNavChange(isNav: Boolean) {
        navImmersive = isNav
        changeListener?.onImmersiveChange()
    }

    override fun onStatusChange(isStatus: Boolean) {
        statusImmersive = isStatus
        changeListener?.onImmersiveChange()
    }

    fun add() {
        try {
            context.wm.addView(vertical, vertical.params)
        } catch (e: Exception) {}
    }

    fun remove() {
        Exception().printStackTrace()
        try {
            context.wm.removeView(vertical)
        } catch (e: Exception) {}
    }

    fun isStatusImmersive() = isFullImmersive() || statusImmersive

    fun isNavImmersive() = isFullImmersive() || navImmersive

    fun isFullImmersive() = fullImmersive
}