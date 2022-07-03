package com.xda.nachonotch.views

import android.content.Context
import android.graphics.Color
import android.view.*
import android.view.WindowManager.LayoutParams.*
import com.xda.nachonotch.util.EnvironmentManager
import com.xda.nachonotch.util.PrefManager
import com.xda.nachonotch.util.environmentManager
import com.xda.nachonotch.util.prefManager

class TopOverlay(context: Context) : BaseOverlay(context, backgroundColor = Color.BLACK) {
    override val params: WindowManager.LayoutParams
        get() = super.params.apply {
            flags = flags or FLAG_DIM_BEHIND
            dimAmount = 0.000001f
            gravity = Gravity.TOP
            width = MATCH_PARENT
            height = context.prefManager.statusBarHeight
        }

    override val listenKeys: List<String>
        get() = listOf(PrefManager.STATUS_HEIGHT)

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