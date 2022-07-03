package com.xda.nachonotch.util

import android.content.Context
import android.database.ContentObserver
import android.graphics.Rect
import android.net.Uri
import android.provider.Settings
import android.view.View
import com.xda.nachonotch.views.immersive.BaseImmersiveHelperView
import com.xda.nachonotch.views.immersive.HorizontalImmersiveHelperView
import com.xda.nachonotch.views.immersive.VerticalImmersiveHelperView
import kotlinx.coroutines.launch

class ImmersiveHelperManager(private val context: Context, private val immersiveListener: ImmersiveChangeListener) : ContentObserver(mainHandler) {
    companion object {
        const val POLICY_CONTROL = "policy_control"
    }

    private val vertical = VerticalImmersiveHelperView(context) { left, top, right, bottom ->
        verticalLayout = Rect(left, top, right, bottom)
    }

    private val horizontal = HorizontalImmersiveHelperView(context) { left, top, right, bottom ->
        horizontalLayout = Rect(left, top, right, bottom)
    }

    private val verticalAttachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View?) {
            verticalHelperAdded = true
        }

        override fun onViewDetachedFromWindow(v: View?) {
            verticalHelperAdded = false
        }
    }
    private val horizontalAttachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View?) {
            horizontalHelperAdded = true
        }

        override fun onViewDetachedFromWindow(v: View?) {
            horizontalHelperAdded = false
        }
    }

    var verticalLayout = Rect()
        set(value) {
            synchronized(this) {
                if (field != value) {
                    field.set(value)

                    updateImmersiveListener()
                }
            }
        }

    var horizontalLayout = Rect()
        set(value) {
            synchronized(this) {
                if (field != value) {
                    field.set(value)

                    updateImmersiveListener()
                }
            }
        }

    var verticalHelperAdded = false
    var horizontalHelperAdded = false

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        when (uri) {
            Settings.Global.getUriFor(POLICY_CONTROL) -> {
                updateImmersiveListener()
            }
        }
    }

    private fun updateImmersiveListener() {
        immersiveListener.onImmersiveChange()
    }

    fun onCreate() {
        context.contentResolver.registerContentObserver(Settings.Global.getUriFor(POLICY_CONTROL), true, this)
        vertical.addOnAttachStateChangeListener(verticalAttachListener)
        horizontal.addOnAttachStateChangeListener(horizontalAttachListener)
    }

    fun add() {
        tryAdd(verticalHelperAdded, vertical)
        tryAdd(horizontalHelperAdded, horizontal)
    }

    private fun tryAdd(alreadyAdded: Boolean, view: BaseImmersiveHelperView) {
        val wm = context.wm

        try {
            if (!alreadyAdded) {
                wm.addView(view, view.params)
            } else {
                wm.updateViewLayout(view, view.params)
            }
        } catch (_: Exception) {}
    }

    fun onDestroy() {
        context.contentResolver.unregisterContentObserver(this)
        vertical.removeOnAttachStateChangeListener(verticalAttachListener)
        horizontal.removeOnAttachStateChangeListener(horizontalAttachListener)
        remove()
    }

    fun remove() {
        tryRemove(vertical)
        tryRemove(horizontal)
    }

    private fun tryRemove(view: BaseImmersiveHelperView) {
        val wm = context.wm

        try {
            wm.removeView(view)
        } catch (_: Exception) {}
    }

    fun isStatusImmersive() = run {
        synchronized(this) {
            val top = verticalLayout.top
            top <= 0 || isFullPolicyControl() || isStatusPolicyControl()
        }
    }

    fun isNavImmersive(callback: (Boolean) -> Unit) {
        logicScope.launch {
            synchronized(this) {
                val screenSize = context.realScreenSize
                val overscan = context.safeOverscanInsets

                val isNav = if (isLandscape) {
                    horizontalLayout.left <= 0 && horizontalLayout.right >= screenSize.x - overscan.bottom
                } else {
                    verticalLayout.bottom >= screenSize.y - overscan.bottom
                }

                mainScope.launch {
                    callback.invoke(isNav || isFullPolicyControl() || isNavPolicyControl())
                }
            }
        }
    }

    fun isFullPolicyControl() = Settings.Global.getString(context.contentResolver, POLICY_CONTROL)?.contains("immersive.full") == true
    fun isNavPolicyControl() = Settings.Global.getString(context.contentResolver, POLICY_CONTROL)?.contains("immersive.nav") == true
    fun isStatusPolicyControl() = Settings.Global.getString(context.contentResolver, POLICY_CONTROL)?.contains("immersive.status") == true
}