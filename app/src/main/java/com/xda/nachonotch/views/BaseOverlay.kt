package com.xda.nachonotch.views

import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.core.animation.addListener
import com.bugsnag.android.Bugsnag
import com.xda.nachonotch.util.EnvironmentManager
import com.xda.nachonotch.util.Event
import com.xda.nachonotch.util.EventObserver
import com.xda.nachonotch.util.environmentManager
import com.xda.nachonotch.util.eventManager
import com.xda.nachonotch.util.prefManager
import com.xda.nachonotch.util.wm

abstract class BaseOverlay(
    context: Context,
    backgroundResource: Int = 0,
    backgroundColor: Int = Int.MIN_VALUE,
) : View(context), EventObserver, OnSharedPreferenceChangeListener {
    open val params = WindowManager.LayoutParams().apply {
        @Suppress("DEPRECATION")
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }

        type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PRIORITY_PHONE
        } else {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }

        alpha = 0f
        format = PixelFormat.RGBA_8888
    }

    protected open val listenKeys: List<String> = listOf()

    protected val shouldAnimate: Boolean
        get() = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f,
        ) > 0.0f

    private var animator: ValueAnimator? = null
        set(value) {
            field?.cancel()
            field = value
        }

    init {
        if (backgroundResource != 0) {
            setBackgroundResource(backgroundResource)
        } else if (backgroundColor != Int.MIN_VALUE) {
            setBackgroundColor(backgroundColor)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isForceDarkAllowed = false
        }
    }

    fun onCreate() {
        context.eventManager.addObserver(this)
        context.prefManager.registerOnSharedPreferenceChangeListener(this)
    }

    fun onDestroy() {
        context.eventManager.removeObserver(this)
        context.prefManager.unregisterOnSharedPreferenceChangeListener(this)
    }

    protected abstract fun canAdd(): Boolean
    protected abstract fun canShow(): Boolean

    override fun onEvent(event: Event) {
        when (event) {
            is Event.EnvironmentStatusUpdated -> onStatusUpdate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        onStatusUpdate()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (listenKeys.contains(key)) {
            onUpdateParams()
            update()
        }
    }

    open fun update() {
        try {
            context.wm.updateViewLayout(this, params)
        } catch (_: Exception) {}
    }

    open fun add() {
        if (canAdd()) {
            Bugsnag.leaveBreadcrumb("Adding ${this::class.java.name}")
            try {
                context.wm.addView(this, params)
            } catch (e: Exception) {
                Bugsnag.notify(IllegalStateException("Error adding ${this::class.java.name}", e))
            }
        }
    }

    open fun remove() {
        Bugsnag.leaveBreadcrumb("Removing ${this::class.java.name}")

        hide {
            try {
                context.wm.removeView(this)
            } catch (e: Exception) {
                Bugsnag.notify(IllegalStateException("Error removing ${this::class.java.name}", e))
            }
        }
    }

    open fun show(animationComplete: (() -> Unit)? = null) {
        Bugsnag.leaveBreadcrumb("Showing ${this::class.java.name}.")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            }
            update()

            if (shouldAnimate) {
                var canceled = false
                animator = ValueAnimator.ofFloat(params.alpha, 1f)
                animator?.addListener(
                    onEnd = {
                        if (!canceled) {
                            animationComplete?.invoke()
                        }
                    },
                    onCancel = { canceled = true },
                )
                animator?.addUpdateListener {
                    params.alpha = it.animatedFraction
                    update()
                }
                animator?.start()
            } else {
                params.alpha = 1f
                update()
                animationComplete?.invoke()
            }
    }

    open fun hide(animationComplete: (() -> Unit)? = null) {
        Bugsnag.leaveBreadcrumb("Hiding ${this::class.java.name}.")

        if (shouldAnimate) {
            var canceled = false
            animator = ValueAnimator.ofFloat(params.alpha, 0f)
            animator?.addListener(
                onEnd = {
                    if (!canceled) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        }
                        update()

                        animationComplete?.invoke()
                    }
                },
                onCancel = { canceled = true },
            )
            animator?.addUpdateListener {
                params.alpha = 1f - it.animatedFraction
                update()
            }
            animator?.start()
        } else {
            params.alpha = 0f
            update()
            animationComplete?.invoke()
        }
    }

    protected open fun onUpdateParams() {}

    final override fun setBackgroundResource(resid: Int) {
        super.setBackgroundResource(resid)
    }

    final override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(color)
    }

    protected fun checkLandscape(): Boolean {
        return !context.environmentManager.environmentStatus.contains(EnvironmentManager.EnvironmentStatus.LANDSCAPE)
    }

    private fun onStatusUpdate() {
        if (canShow()) {
            show()
        } else {
            hide()
        }
    }
}