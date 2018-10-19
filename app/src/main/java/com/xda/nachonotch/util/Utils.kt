package com.xda.nachonotch.util

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.TypedValue
import android.view.WindowManager
import android.widget.Toast
import com.xda.nachonotch.R
import com.xda.nachonotch.activities.TermsActivity
import com.xda.nachonotch.services.BackgroundHandler

/**
 * General utility functions for OHM
 */
object Utils {
    val IMMERSIVE_STATUS = 4
    val IMMERSIVE_NAV = 2
    val IMMERSIVE_FULL = IMMERSIVE_STATUS or IMMERSIVE_NAV

    val TERMS_VERSION = 1

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
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt("status_height", getResurceStatusHeight(context))
    }

    fun getResurceStatusHeight(context: Context): Int {
        return context.resources.getDimensionPixelSize(context.resources.getIdentifier("status_bar_height", "dimen", "android"))
    }

    fun isEnabled(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(BackgroundHandler.SHOULD_RUN, false)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(BackgroundHandler.SHOULD_RUN, enabled).apply()
    }

    fun areTopCornersEnabled(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("rounded_corners", false)
    }

    fun areBottomCornersEnabled(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("rounded_corners_bottom", false)
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
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt("nav_height", getResourceNavHeight(context))
    }

    fun getResourceNavHeight(context: Context): Int {
        return if (hasNavBar(context))
            context.resources.getDimensionPixelSize(context.resources.getIdentifier("navigation_bar_height", "dimen", "android")) else 0
    }

    fun launchOverlaySettings(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
        context.startActivity(intent)

        Toast.makeText(context, R.string.enable_overlay_permission, Toast.LENGTH_SHORT).show()
    }

    fun enforceTerms(context: Context): Boolean {
        val termsVer = PreferenceManager.getDefaultSharedPreferences(context).getInt("terms_version", 0)
        return if (termsVer < TERMS_VERSION) {
            context.startActivity(Intent(context, TermsActivity::class.java))
            false
        } else true

    }

    fun getTopCornerWidthDp(context: Context) =
            PreferenceManager.getDefaultSharedPreferences(context)
                    .getInt("top_corner_width", 24)

    fun getTopCornerWidthPx(context: Context) = dpAsPx(context, getTopCornerWidthDp(context))

    fun getTopCornerHeightDp(context: Context) =
            PreferenceManager.getDefaultSharedPreferences(context)
                    .getInt("top_corner_height", 24)

    fun getTopCornerHeightPx(context: Context) = dpAsPx(context, getTopCornerHeightDp(context))

    fun getBottomCornerWidthDp(context: Context) =
            PreferenceManager.getDefaultSharedPreferences(context)
                    .getInt("bottom_corner_width", 24)

    fun getBottomCornerWidthPx(context: Context) = dpAsPx(context, getBottomCornerWidthDp(context))

    fun getBottomCornerHeightDp(context: Context) =
            PreferenceManager.getDefaultSharedPreferences(context)
                    .getInt("bottom_corner_height", 24)

    fun getBottomCornerHeightPx(context: Context) = dpAsPx(context, getBottomCornerHeightDp(context))
}