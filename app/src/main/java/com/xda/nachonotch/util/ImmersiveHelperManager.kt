package com.xda.nachonotch.util

import android.content.Context
import android.database.ContentObserver
import android.graphics.Rect
import android.net.Uri
import android.provider.Settings
import android.view.ViewTreeObserver
import com.xda.nachonotch.views.immersive.ImmersiveHelperViewHorizontal
import com.xda.nachonotch.views.immersive.ImmersiveHelperViewVertical
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

class ImmersiveHelperManager(private val context: Context) : ContentObserver(mainHandler) {
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
    var verticalHelperAdded = false

    var immersiveListener: ImmersiveChangeListener? = null

    init {
        horizontal.immersiveListener = { left, top, right, bottom ->
            horizontalLayout = Rect(left, top, right, bottom)
        }
        vertical.immersiveListener = { left, top, right, bottom ->
            verticalLayout = Rect(left, top, right, bottom)
        }
    }

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

        context.contentResolver.unregisterContentObserver(this)

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
        top <= 0 || isFullPolicyControl() || isStatusPolicyControl()
    }

    fun isNavImmersive(callback: (Boolean) -> Unit) {
        logicScope.launch {
            val screenSize = context.realScreenSize
            val overscan = Rect().apply { context.wm.defaultDisplay.getOverscanInsets(this) }

            val isNav = if (isLandscape) {
                horizontalLayout.left <= 0 && horizontalLayout.right >= screenSize.x + if (overscan.right < 0) overscan.bottom else 0
            } else {
                verticalLayout.bottom >= screenSize.y + if (overscan.bottom < 0) overscan.bottom else 0
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