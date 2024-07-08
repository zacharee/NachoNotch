package com.xda.nachonotch.views.immersive

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.view.Gravity
import android.view.Surface
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.xda.nachonotch.util.LoggingBugsnag
import com.xda.nachonotch.util.cachedRotation
import com.xda.nachonotch.util.mainScope
import com.xda.nachonotch.util.wm
import kotlinx.coroutines.launch

@SuppressLint("ViewConstructor")
@Suppress("DEPRECATION")
open class BaseImmersiveHelperView(
    context: Context,
    private val immersiveListener: (nav: Boolean?, status: Boolean?) -> Unit,
    private val layoutListener: (left: Int, top: Int, right: Int, bottom: Int) -> Unit,
    private val isVertical: Boolean,
) : View(context) {
    @SuppressLint("RtlHardcoded")
    val params = WindowManager.LayoutParams().apply {
        type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_PHONE
        } else {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_ATTACHED_IN_DECOR
        }

        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        format = PixelFormat.TRANSPARENT
        x = 0
        y = 0
        gravity = Gravity.LEFT or Gravity.BOTTOM

        height = WindowManager.LayoutParams.MATCH_PARENT
        width = WindowManager.LayoutParams.MATCH_PARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.fitInsetsSides = 0
            this.fitInsetsTypes = 0
        }
    }

    init {
        alpha = 0f
        fitsSystemWindows = Build.VERSION.SDK_INT < Build.VERSION_CODES.R

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isForceDarkAllowed = false
        }
    }

    private val rect = Rect()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        handleInsets(rootWindowInsets)

        getBoundsOnScreen(rect)

        LoggingBugsnag.leaveBreadcrumb("Laying out ${this::class.java.name} with insets $rootWindowInsets and layout ${rect}.")

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            mainScope.launch {
                var realTop = rect.top

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    val insetsTop = WindowInsetsCompat.toWindowInsetsCompat(rootWindowInsets).stableInsets.top

                    if (realTop == 0 && insetsTop > 0) {
                        realTop = insetsTop
                    }
                }

                layoutListener(rect.left, realTop, rect.right, rect.bottom)
            }
        }

        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        updateDimensions()
        updateLayout()

        rootView.setOnApplyWindowInsetsListener { _, insets ->
            if (!rootWindowInsets.isConsumed) {
                LoggingBugsnag.leaveBreadcrumb("Applying window insets $rootWindowInsets for ${this::class.java.name}.")
                handleInsets(rootWindowInsets)
            }

            insets
        }
    }

    open fun updateDimensions() {}

    private fun handleInsets(insets: WindowInsets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val compat = WindowInsetsCompat.toWindowInsetsCompat(insets)

            val navShowing = compat.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars()) != Insets.NONE
            val statusShowing = compat.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars()) != Insets.NONE

            mainScope.launch {
                immersiveListener(
                    if ((cachedRotation == Surface.ROTATION_0 || cachedRotation == Surface.ROTATION_180) && !isVertical) null else !navShowing,
                    if (isVertical) !statusShowing else null,
                )
            }
        }
    }

    private fun updateLayout() {
        mainScope.launch {
            try {
                context.wm.updateViewLayout(this@BaseImmersiveHelperView, params)
            } catch (_: Exception) {
            }
        }
    }
}