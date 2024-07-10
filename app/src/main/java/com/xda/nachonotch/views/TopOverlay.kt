package com.xda.nachonotch.views

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.view.*
import android.view.WindowManager.LayoutParams.*
import com.xda.nachonotch.util.EnvironmentManager
import com.xda.nachonotch.util.PrefManager
import com.xda.nachonotch.util.environmentManager
import com.xda.nachonotch.util.prefManager

class TopOverlay(context: Context) : BaseOverlay(context, backgroundColor = Color.BLACK) {
    override val params: WindowManager.LayoutParams = super.params.apply {
        flags = flags or if (context.prefManager.forceLightStatusBarIcons) FLAG_DIM_BEHIND else 0
        dimAmount = 0.000001f
        gravity = Gravity.TOP
        width = MATCH_PARENT
        height = context.prefManager.statusBarHeight
    }

    override val listenKeys: List<String>
        get() = listOf(PrefManager.STATUS_HEIGHT)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)

        when (key) {
            PrefManager.FORCE_LIGHT_STATUS_BAR_ICONS -> {
                val forceLightIcons = context.prefManager.forceLightStatusBarIcons
                params.flags = if (forceLightIcons) {
                    params.flags or FLAG_DIM_BEHIND
                } else {
                    params.flags and FLAG_DIM_BEHIND.inv()
                }
                params.dimAmount = if (forceLightIcons) 0.000001f else 0f

                hide {
                    show()
                }
            }
        }
    }

    override fun onUpdateParams() {
        params.height = context.prefManager.statusBarHeight
    }

    override fun canAdd(): Boolean {
        return context.prefManager.isEnabled
    }

    override fun canShow(): Boolean {
        return !context.environmentManager.environmentStatus.contains(EnvironmentManager.EnvironmentStatus.STATUS_IMMERSIVE)
                && checkLandscape()
    }
}