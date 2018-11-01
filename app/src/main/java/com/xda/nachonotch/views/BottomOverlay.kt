package com.xda.nachonotch.views

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.xda.nachonotch.util.Utils

class BottomOverlay : View {
    private val wm: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        setBackgroundColor(Color.BLACK)
    }

    fun isHidden(): Boolean {
        return isHidden(systemUiVisibility)
    }

    fun isHidden(vis: Int): Boolean {
        val immersive = Settings.Global.getString(context.contentResolver, Settings.Global.POLICY_CONTROL) ?: "immersive.none"
        val overscan = Rect()

        wm.defaultDisplay.getOverscanInsets(overscan)

        return vis and Utils.IMMERSIVE_NAV != 0 || immersive.contains("full") || immersive.contains("navigation") || overscan.bottom < 0
    }

    fun getParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams().apply {
            flags  = WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
            type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_PHONE else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            gravity = Gravity.BOTTOM
            width =  WindowManager.LayoutParams.MATCH_PARENT
            height = Utils.getNavBarHeight(context)
            y = -Utils.getResourceNavHeight(context)
            format = PixelFormat.TRANSLUCENT
        }
    }
}