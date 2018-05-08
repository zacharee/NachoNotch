package com.xda.nachonotch.util

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.preference.PreferenceManager
import android.util.TypedValue
import android.view.WindowManager
import com.xda.nachonotch.services.BackgroundHandler

/**
 * General utility functions for OHM
 */
object Utils {
    val IMMERSIVE_STATUS = 4
    val IMMERSIVE_NAV = 2
    val IMMERSIVE_FULL = IMMERSIVE_STATUS or IMMERSIVE_NAV

    /**
     * Get the device's screen size
     * @param context context object
     * @return device's resolution (in px) as a Point
     */
    fun getRealScreenSize(context: Context): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()

        display.getRealSize(size)

        return size
    }

    /**
     * Convert a certain DP value to its equivalent in px
     * @param context context object
     * @param dpVal the chosen DP value
     * @return the DP value in terms of px
     */
    fun dpAsPx(context: Context, dpVal: Int): Int {
        val r = context.resources
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal.toFloat(), r.displayMetrics))
    }

    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun isEnabled(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(BackgroundHandler.SHOULD_RUN, false)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(BackgroundHandler.SHOULD_RUN, enabled).apply()
    }

    fun areCornersEnabled(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("rounded_corners", false)
    }

    fun isNavCoverEnabled(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("cover_nav", false)
    }

    fun hasNavBar(context: Context): Boolean {
        val id = context.resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && context.resources.getBoolean(id) || Build.MODEL.contains("Android SDK built for x86")
    }

    /**
     * Get the height of the navigation bar
     * @param context context object
     * @return the height of the navigation bar
     */
    fun getNavBarHeight(context: Context): Int {
        return if (hasNavBar(context)) context.resources.getDimensionPixelSize(context.resources.getIdentifier("navigation_bar_height", "dimen", "android")) else 0
    }
}