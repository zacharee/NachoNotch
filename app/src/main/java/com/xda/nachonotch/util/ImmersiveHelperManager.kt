package com.xda.nachonotch.util

import android.content.Context
import android.database.ContentObserver
import android.graphics.Rect
import android.net.Uri
import android.provider.Settings
import android.view.ViewTreeObserver
import com.xda.nachonotch.views.immersive.BaseImmersiveHelperView
import kotlinx.coroutines.launch

class ImmersiveHelperManager(private val context: Context) : ContentObserver(mainHandler) {
    val base = BaseImmersiveHelperView(context, this) { left, top, right, bottom ->
        layout = Rect(left, top, right, bottom)
    }

    var layout = Rect()
        set(value) {
            if (field != value) {
                field.set(value)

                updateImmersiveListener()
            }
        }

    var helperAdded = false

    var immersiveListener: ImmersiveChangeListener? = null

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        when (uri) {
            Settings.Global.getUriFor(Settings.Global.POLICY_CONTROL) -> {
                updateImmersiveListener()
            }
        }
    }

    private fun updateImmersiveListener() {
        immersiveListener?.onImmersiveChange()
    }

    fun add() {
        val wm = context.wm

        context.contentResolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.POLICY_CONTROL), true, this)

        try {
            if (!helperAdded) {
                wm.addView(base, base.params)
            } else {
                wm.updateViewLayout(base, base.params)
            }
        } catch (e: Exception) {}
    }

    fun remove() {
        val wm = context.wm

        context.contentResolver.unregisterContentObserver(this)

        try {
            wm.removeView(base)
        } catch (e: Exception) {}
    }

    fun addOnGlobalLayoutListener(listener: ViewTreeObserver.OnGlobalLayoutListener) {
        base.viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    fun enterNavImmersive() {
        base.enterNavImmersive()
    }

    fun exitNavImmersive() {
        base.exitNavImmersive()
    }

    fun isStatusImmersive() = run {
        val top = layout.top
        top <= 0 || isFullPolicyControl() || isStatusPolicyControl()
    }

    fun isNavImmersive(callback: (Boolean) -> Unit) {
        logicScope.launch {
            val screenSize = context.realScreenSize
            val overscan = Rect().apply { context.wm.defaultDisplay.getOverscanInsets(this) }

            val isNav = if (isLandscape) {
                layout.left <= 0 && layout.right >= screenSize.x - overscan.bottom
            } else {
                layout.bottom >= screenSize.y - overscan.bottom
            }

            mainScope.launch {
                callback.invoke(isNav || isFullPolicyControl() || isNavPolicyControl())
            }
        }
    }

    fun isFullPolicyControl() = Settings.Global.getString(context.contentResolver, Settings.Global.POLICY_CONTROL)?.contains("immersive.full") == true
    fun isNavPolicyControl() = Settings.Global.getString(context.contentResolver, Settings.Global.POLICY_CONTROL)?.contains("immersive.nav") == true
    fun isStatusPolicyControl() = Settings.Global.getString(context.contentResolver, Settings.Global.POLICY_CONTROL)?.contains("immersive.status") == true
}