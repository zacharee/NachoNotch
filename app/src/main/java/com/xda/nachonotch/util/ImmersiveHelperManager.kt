package com.xda.nachonotch.util

import android.content.Context
import android.graphics.Rect
import android.view.ViewTreeObserver
import com.xda.nachonotch.views.immersive.ImmersiveHelperViewHorizontal
import com.xda.nachonotch.views.immersive.ImmersiveHelperViewVertical

class ImmersiveHelperManager(private val context: Context) {
    val horizontal = ImmersiveHelperViewHorizontal(context, this)
    val vertical = ImmersiveHelperViewVertical(context, this)

    var horizontalLayout = Rect()
        set(value) {
            if (field != value) {
                field.set(value)

                updateImmersiveListener()
            }
        }

    var verticalLayout = Rect()
        set(value) {
            if (field != value) {
                field.set(value)

                updateImmersiveListener()
            }
        }

    var horizontalHelperAdded = false
        set(value) {
            field = value

            updateHelperState()
        }
    var verticalHelperAdded = false
        set(value) {
            field = value

            updateHelperState()
        }


    var immersiveListener: ImmersiveChangeListener? = null

    init {
        horizontal.immersiveListener = { left, top, right, bottom ->
            horizontalLayout = Rect(left, top, right, bottom)
        }
        vertical.immersiveListener = { left, top, right, bottom ->
            verticalLayout = Rect(left, top, right, bottom)
        }
    }

    private fun updateImmersiveListener() {
        immersiveListener?.onImmersiveChange()
    }

    private fun updateHelperState() {
    }

    fun add() {
        val wm = context.wm

        try {
            if (!horizontalHelperAdded) {
                wm.addView(horizontal, horizontal.params)
            } else {
                wm.updateViewLayout(horizontal, horizontal.params)
            }
        } catch (e: Exception) {}

        try {
            if (!verticalHelperAdded) {
                wm.addView(vertical, vertical.params)
            } else {
                wm.updateViewLayout(vertical, vertical.params)
            }
        } catch (e: Exception) {}
    }

    fun remove() {
        val wm = context.wm

        try {
            wm.removeView(horizontal)
        } catch (e: Exception) {}

        try {
            wm.removeView(vertical)
        } catch (e: Exception) {}
    }

    fun addOnGlobalLayoutListener(listener: ViewTreeObserver.OnGlobalLayoutListener) {
        horizontal.viewTreeObserver.addOnGlobalLayoutListener(listener)
        vertical.viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    fun enterNavImmersive() {
        horizontal.enterNavImmersive()
        vertical.enterNavImmersive()
    }

    fun exitNavImmersive() {
        horizontal.exitNavImmersive()
        vertical.exitNavImmersive()
    }

    fun isStatusImmersive() = run {
        val top = verticalLayout.top
        top <= 0
    }

    fun isNavImmersive() = run {
        if (isLandscape) {
            horizontalLayout.left <= 0 && horizontalLayout.right <= 0
        } else {
            verticalLayout.bottom <= 0
        }
    }
}