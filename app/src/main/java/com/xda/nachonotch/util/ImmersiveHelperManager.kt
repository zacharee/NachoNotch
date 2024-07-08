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

class ImmersiveHelperManager(
    private val context: Context,
    private val immersiveListener: ImmersiveChangeListener,
) : ContentObserver(mainHandler) {
    companion object {
        const val POLICY_CONTROL = "policy_control"
    }

    private val vertical = VerticalImmersiveHelperView(
        context = context,
        immersiveListener = { nav, status ->
            _isNavImmersive = nav ?: _isNavImmersive
            _isStatusImmersive = status ?: _isStatusImmersive
        },
        layoutListener = { left, top, right, bottom ->
            verticalLayout = Rect(left, top, right, bottom)
        },
    )

    private val horizontal = HorizontalImmersiveHelperView(
        context = context,
        immersiveListener = { nav, status ->
            _isNavImmersive = nav ?: _isNavImmersive
            _isStatusImmersive = status ?: _isStatusImmersive
        },
        layoutListener = { left, top, right, bottom ->
            horizontalLayout = Rect(left, top, right, bottom)
        },
    )

    private val verticalAttachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View?) {
            LoggingBugsnag.leaveBreadcrumb("Vertical helper attached.")
            verticalHelperAdded = true
        }

        override fun onViewDetachedFromWindow(v: View?) {
            LoggingBugsnag.leaveBreadcrumb("Vertical helper detached.")
            verticalHelperAdded = false
        }
    }
    private val horizontalAttachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View?) {
            LoggingBugsnag.leaveBreadcrumb("Horizontal helper attached.")
            horizontalHelperAdded = true
        }

        override fun onViewDetachedFromWindow(v: View?) {
            LoggingBugsnag.leaveBreadcrumb("Horizontal helper detached.")
            horizontalHelperAdded = false
        }
    }

    var verticalLayout: Rect? = null
        set(value) {
            synchronized(this) {
                if (field != value) {
                    field = value

                    LoggingBugsnag.leaveBreadcrumb("New vertical layout $value. Screen size ${context.realScreenSize}. Overscan ${context.safeOverscanInsets}.")

                    updateImmersiveListener()
                }
            }
        }

    var horizontalLayout: Rect? = null
        set(value) {
            synchronized(this) {
                if (field != value) {
                    field = value

                    LoggingBugsnag.leaveBreadcrumb("New horizontal layout $value. Screen size ${context.realScreenSize}. Overscan ${context.safeOverscanInsets}.")

                    updateImmersiveListener()
                }
            }
        }

    private var _isNavImmersive = false
        set(value) {
            synchronized(this) {
                if (field != value) {
                    field = value

                    LoggingBugsnag.leaveBreadcrumb("New nav immersive state $value.")

                    updateImmersiveListener()
                }
            }
        }

    private var _isStatusImmersive = false
        set(value) {
            synchronized(this) {
                if (field != value) {
                    field = value

                    LoggingBugsnag.leaveBreadcrumb("New status immersive state $value.")

                    updateImmersiveListener()
                }
            }
        }

    var verticalHelperAdded = false
    var horizontalHelperAdded = false

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        when (uri) {
            Settings.Global.getUriFor(POLICY_CONTROL) -> {
                LoggingBugsnag.leaveBreadcrumb(
                    "Policy control changed. ${
                        Settings.Global.getString(
                            context.contentResolver,
                            POLICY_CONTROL,
                        )
                    }"
                )
                updateImmersiveListener()
            }
        }
    }

    private fun updateImmersiveListener() {
        LoggingBugsnag.leaveBreadcrumb("Updating immersive listeners.")
        immersiveListener.onImmersiveChange()
    }

    fun onCreate() {
        context.contentResolver.registerContentObserver(
            Settings.Global.getUriFor(POLICY_CONTROL),
            true,
            this
        )
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
        } catch (e: Exception) {
            LoggingBugsnag.leaveBreadcrumb("Unable to add ${view::class.java.name}, alreadyAdded: ${alreadyAdded}: ${e.message}")
        }
    }

    fun onDestroy() {
        remove()
        context.contentResolver.unregisterContentObserver(this)
        vertical.removeOnAttachStateChangeListener(verticalAttachListener)
        horizontal.removeOnAttachStateChangeListener(horizontalAttachListener)
    }

    fun remove() {
        tryRemove(vertical)
        tryRemove(horizontal)
    }

    private fun tryRemove(view: BaseImmersiveHelperView) {
        val wm = context.wm

        try {
            wm.removeView(view)
        } catch (e: Exception) {
            LoggingBugsnag.leaveBreadcrumb("Unable to remove ${view::class.java.name}: ${e.message}")
        }
    }

    fun isStatusImmersive() = run {
        synchronized(this) {
            val top = verticalLayout?.top ?: Int.MAX_VALUE
            top <= 0 || isFullPolicyControl() || isStatusPolicyControl() || _isStatusImmersive
        }
    }

    fun isNavImmersive(callback: (Boolean) -> Unit) {
        logicScope.launch {
            synchronized(this) {
                val screenSize = context.realScreenSize
                val overscan = context.safeOverscanInsets

                val isNav = if (isLandscape) {
                    (horizontalLayout?.left ?: Int.MAX_VALUE) <= 0 && (horizontalLayout?.right ?: Int.MIN_VALUE) >= screenSize.x - overscan.bottom
                } else {
                    (verticalLayout?.bottom ?: Int.MIN_VALUE) >= screenSize.y - overscan.bottom
                }

                mainScope.launch {
                    callback.invoke(isNav || isFullPolicyControl() || isNavPolicyControl() || _isNavImmersive)
                }
            }
        }
    }

    fun isFullPolicyControl() = Settings.Global.getString(context.contentResolver, POLICY_CONTROL)
        ?.contains("immersive.full") == true

    fun isNavPolicyControl() = Settings.Global.getString(context.contentResolver, POLICY_CONTROL)
        ?.contains("immersive.nav") == true

    fun isStatusPolicyControl() = Settings.Global.getString(context.contentResolver, POLICY_CONTROL)
        ?.contains("immersive.status") == true
}