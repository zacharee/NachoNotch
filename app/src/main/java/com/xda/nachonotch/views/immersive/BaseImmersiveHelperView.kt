package com.xda.nachonotch.views.immersive

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.view.WindowInsetsCompat
import com.xda.nachonotch.util.mainScope
import com.xda.nachonotch.util.wm
import kotlinx.coroutines.launch

@SuppressLint("ViewConstructor")
@Suppress("DEPRECATION")
open class BaseImmersiveHelperView(
    context: Context,
    private val immersiveListener: (nav: Boolean, status: Boolean) -> Unit,
    private val layoutListener: (left: Int, top: Int, right: Int, bottom: Int) -> Unit,
) : View(context) {
    @SuppressLint("RtlHardcoded")
    val params = WindowManager.LayoutParams().apply {
        type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_PHONE
        else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        format = PixelFormat.TRANSPARENT
        x = 0
        y = 0
        gravity = Gravity.LEFT or Gravity.BOTTOM

        height = WindowManager.LayoutParams.MATCH_PARENT
        width = WindowManager.LayoutParams.MATCH_PARENT
    }

    init {
        alpha = 0f
        fitsSystemWindows = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isForceDarkAllowed = false
        }
    }

    private val rect = Rect()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        handleInsets(rootWindowInsets)

        getBoundsOnScreen(rect)

        mainScope.launch {
            layoutListener(rect.left, rect.top, rect.right, rect.bottom)
        }

        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        updateDimensions()
        updateLayout()

        setOnApplyWindowInsetsListener { _, insets ->
            handleInsets(insets)

            insets
        }
    }

    open fun updateDimensions() {}

    private fun handleInsets(insets: WindowInsets) {
        val compat = WindowInsetsCompat.toWindowInsetsCompat(insets)

        val navShowing = compat.isVisible(WindowInsetsCompat.Type.navigationBars())
        val statusShowing = compat.isVisible(WindowInsetsCompat.Type.statusBars())

        mainScope.launch {
            immersiveListener(!navShowing, !statusShowing)
        }
    }

    private fun updateLayout() {
        mainScope.launch {
            try {
                context.wm.updateViewLayout(this@BaseImmersiveHelperView, params)
            } catch (_: Exception) {}
        }
    }
}