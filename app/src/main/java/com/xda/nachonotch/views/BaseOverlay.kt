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
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.animation.addListener
import com.xda.nachonotch.util.EnvironmentManager
import com.xda.nachonotch.util.Event
import com.xda.nachonotch.util.EventObserver
import com.xda.nachonotch.util.LoggingBugsnag
import com.xda.nachonotch.util.environmentManager
import com.xda.nachonotch.util.eventManager
import com.xda.nachonotch.util.prefManager
import com.xda.nachonotch.util.wm

abstract class BaseOverlay(
    context: Context,
    @DrawableRes
    backgroundResource: Int? = null,
    @ColorInt
    backgroundColor: Int? = null,
) : View(context), EventObserver, OnSharedPreferenceChangeListener {
    companion object {
        const val FULL_ALPHA = 1f
    }

    open val params = WindowManager.LayoutParams().apply {
        @Suppress("DEPRECATION")
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

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
        if (backgroundResource != null) {
            setBackgroundResource(backgroundResource)
        } else if (backgroundColor != null) {
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
        } catch (_: Exception) {
        }
    }

    open fun add() {
        if (canAdd()) {
            LoggingBugsnag.leaveBreadcrumb("Adding ${this::class.java.name}")
            try {
                context.wm.addView(this, params)
            } catch (e: Exception) {
                LoggingBugsnag.leaveBreadcrumb("Error adding ${this::class.java.name}.", error = e)
            }
        }
    }

    open fun remove(finishListener: (() -> Unit)? = null) {
        LoggingBugsnag.leaveBreadcrumb("Removing ${this::class.java.name}")

        if (isAttachedToWindow) {
            hide {
                try {
                    if (isAttachedToWindow) {
                        context.wm.removeView(this)
                    }
                } catch (e: Exception) {
                    LoggingBugsnag.leaveBreadcrumb("Error removing ${this::class.java.name}.", error = e)
                } finally {
                    finishListener?.invoke()
                }
            }
        } else {
            finishListener?.invoke()
        }
    }

    open fun show(animationComplete: (() -> Unit)? = null) {
        LoggingBugsnag.leaveBreadcrumb("Showing ${this::class.java.name}.")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        }
        update()

        if (shouldAnimate) {
            var canceled = false
            animator = ValueAnimator.ofFloat(params.alpha, FULL_ALPHA)
            animator?.addListener(
                onEnd = {
                    if (!canceled) {
                        animationComplete?.invoke()
                    }
                },
                onCancel = { canceled = true },
            )
            animator?.addUpdateListener {
                params.alpha = it.animatedFraction * FULL_ALPHA
                update()
            }
            animator?.start()
        } else {
            params.alpha = FULL_ALPHA
            update()
            animationComplete?.invoke()
        }
    }

    open fun hide(animationComplete: (() -> Unit)? = null) {
        LoggingBugsnag.leaveBreadcrumb("Hiding ${this::class.java.name}.")

        if (shouldAnimate) {
            var canceled = false
            animator = ValueAnimator.ofFloat(params.alpha, 0f)
            animator?.addListener(
                onEnd = {
                    if (!canceled) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            params.flags =
                                params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        }
                        update()

                        animationComplete?.invoke()
                    }
                },
                onCancel = { canceled = true },
            )
            animator?.addUpdateListener {
                params.alpha = FULL_ALPHA - it.animatedFraction
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
        return !context.environmentManager.hasAllStatuses(EnvironmentManager.EnvironmentStatus.LANDSCAPE)
    }

    private fun onStatusUpdate() {
        if (canShow()) {
            onUpdateParams()
            show()
        } else {
            hide()
        }
    }
}