package com.xda.nachonotch.util

import android.content.Context
import android.database.ContentObserver
import android.graphics.Rect
import android.net.Uri
import android.provider.Settings
import com.xda.nachonotch.views.immersive.BaseImmersiveHelperView
import com.xda.nachonotch.views.immersive.HorizontalImmersiveHelperView
import com.xda.nachonotch.views.immersive.VerticalImmersiveHelperView
import kotlinx.coroutines.launch

class ImmersiveHelperManager(private val context: Context) : ContentObserver(mainHandler) {
    companion object {
        const val POLICY_CONTROL = "policy_control"
    }

    private val vertical = VerticalImmersiveHelperView(context, this) { left, top, right, bottom ->
        verticalLayout = Rect(left, top, right, bottom)
    }

    private val horizontal = HorizontalImmersiveHelperView(context, this) { left, top, right, bottom ->
        horizontalLayout = Rect(left, top, right, bottom)
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

    var immersiveListener: ImmersiveChangeListener? = null

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        when (uri) {
            Settings.Global.getUriFor(POLICY_CONTROL) -> {
                updateImmersiveListener()
            }
        }
    }

    private fun updateImmersiveListener() {
        immersiveListener?.onImmersiveChange()
    }

    fun add() {
        context.contentResolver.registerContentObserver(Settings.Global.getUriFor(POLICY_CONTROL), true, this)

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
        } catch (e: Exception) {}
    }

    fun remove() {
        context.contentResolver.unregisterContentObserver(this)

        tryRemove(vertical)
        tryRemove(horizontal)
    }

    private fun tryRemove(view: BaseImmersiveHelperView) {
        val wm = context.wm

        try {
            wm.removeView(view)
        } catch (e: Exception) {}
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
                val overscan = Rect().apply { context.wm.defaultDisplay.getOverscanInsets(this) }

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