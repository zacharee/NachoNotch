package com.xda.nachonotch.views

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.WindowManager
import com.xda.nachonotch.util.*

class BottomOverlay(context: Context) : BaseOverlay(context, backgroundColor = Color.BLACK) {
    @Suppress("DEPRECATION")
    override val params: WindowManager.LayoutParams = super.params.apply {
        flags = flags or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        gravity = Gravity.BOTTOM
        width = WindowManager.LayoutParams.MATCH_PARENT
        height = context.prefManager.navBarHeight
        y = -context.prefManager.navBarHeight
    }

    override val listenKeys: List<String>
        get() = listOf(PrefManager.NAV_HEIGHT)

    override fun onUpdateParams() {
        val height = context.prefManager.navBarHeight.coerceAtLeast(1)

        params.height = height
        params.y = -height
    }

    override fun canAdd(): Boolean {
        return context.prefManager.coverNav
    }

    override fun canShow(): Boolean {
        return !context.environmentManager.hasAllStatuses(EnvironmentManager.EnvironmentStatus.NAV_IMMERSIVE)
                && checkLandscape()
    }
}