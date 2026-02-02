package com.xda.nachonotch.views

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND
import android.view.WindowManager.LayoutParams.MATCH_PARENT
import com.xda.nachonotch.services.BackgroundHandler
import com.xda.nachonotch.util.EnvironmentManager
import com.xda.nachonotch.util.PrefManager
import com.xda.nachonotch.util.environmentManager
import com.xda.nachonotch.util.prefManager
import com.xda.nachonotch.util.startService

class TopOverlay(context: Context) : BaseOverlay(context, backgroundColor = Color.BLACK) {
    override val params: WindowManager.LayoutParams = super.params.apply {
        gravity = Gravity.TOP
        width = MATCH_PARENT
        height = context.prefManager.statusBarHeight

        updateLightIconsState()
    }

    override val listenKeys: List<String>
        get() = listOf(PrefManager.STATUS_HEIGHT)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)

        when (key) {
            PrefManager.FORCE_LIGHT_STATUS_BAR_ICONS -> {
                params.updateLightIconsState()

                hide {
                    show()
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!BackgroundHandler.isCreated) {
            context.startService()
        }
    }

    override fun onUpdateParams() {
        params.height = context.prefManager.statusBarHeight
    }

    override fun canAdd(): Boolean {
        return context.prefManager.isEnabled
    }

    override fun canShow(): Boolean {
        return !context.environmentManager.hasAllStatuses(EnvironmentManager.EnvironmentStatus.STATUS_IMMERSIVE)
                && checkLandscape()
    }

    private fun WindowManager.LayoutParams.updateLightIconsState() {
        val forceLightIcons = context.prefManager.forceLightStatusBarIcons
        val useZeroDimAmount = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM

        flags = if (forceLightIcons) {
            flags or FLAG_DIM_BEHIND
        } else {
            flags and FLAG_DIM_BEHIND.inv()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            insetsFlags.appearance = if (forceLightIcons) {
                insetsFlags.appearance or WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            } else {
                insetsFlags.appearance and WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS.inv()
            }
        }

        dimAmount = if (forceLightIcons && !useZeroDimAmount) 0.000001f else 0f
    }
}