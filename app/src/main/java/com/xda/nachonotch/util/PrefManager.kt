package com.xda.nachonotch.util

import android.annotation.SuppressLint
import android.content.Context
import androidx.preference.PreferenceManager
import com.xda.nachonotch.services.BackgroundHandler

class PrefManager private constructor(private val context: Context) {
    companion object {
        const val COVER_NAV = "cover_nav"
        const val NAV_HEIGHT = "nav_height"
        const val ROUNDED_CORNERS_BOTTOM = "rounded_corners_bottom"
        const val ROUNDED_CORNERS_TOP = "rounded_corners"
        const val SHOULD_RUN = BackgroundHandler.SHOULD_RUN
        const val STATUS_HEIGHT = "status_height"
        const val TERMS_VERSION = "terms_version"
        const val BOTTOM_CORNER_HEIGHT = "bottom_corner_height"
        const val BOTTOM_CORNER_WIDTH = "bottom_corner_width"
        const val TOP_CORNER_HEIGHT = "top_corner_height"
        const val TOP_CORNER_WIDTH = "top_corner_width"

        @SuppressLint("StaticFieldLeak")
        private var instance: PrefManager? = null

        fun getInstance(context: Context): PrefManager {
            if (instance == null) instance = PrefManager(context.applicationContext)

            return instance!!
        }
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val cornerHeightBottomDp: Int
        get() = getInt(BOTTOM_CORNER_HEIGHT, 24)

    val cornerHeightBottomPx: Int
        get() = context.dpAsPx(cornerHeightBottomDp)

    val cornerWidthBottomDp: Int
        get() = getInt(BOTTOM_CORNER_WIDTH, 24)

    val cornerWidthBottomPx: Int
        get() = context.dpAsPx(cornerWidthBottomDp)

    val cornerHeightTopDp: Int
        get() = getInt(TOP_CORNER_HEIGHT, 24)

    val cornerHeightTopPx: Int
        get() = context.dpAsPx(cornerHeightTopDp)

    val cornerWidthTopDp: Int
        get() = getInt(TOP_CORNER_WIDTH, 24)

    val cornerWidthTopPx: Int
        get() = context.dpAsPx(cornerWidthTopDp)

    val coverNav: Boolean
        get() = getBoolean(COVER_NAV, false)

    var isEnabled: Boolean
        get() = getBoolean(SHOULD_RUN, false)
        set(value) {
            putBoolean(SHOULD_RUN, value)
        }

    val navBarHeight: Int
        get() = getInt(NAV_HEIGHT, context.resourceNavBarHeight)

    val statusBarHeight: Int
        get() = getInt(STATUS_HEIGHT, context.resourceStatusBarHeight)

    val termsVersion: Int
        get() = getInt(TERMS_VERSION, 0)

    val useBottomCorners: Boolean
        get() = getBoolean(ROUNDED_CORNERS_BOTTOM, false)

    val useTopCorners: Boolean
        get() = getBoolean(ROUNDED_CORNERS_TOP, false)

    fun getBoolean(key: String, def: Boolean) = prefs.getBoolean(key, def)
    fun getFloat(key: String, def: Float) = prefs.getFloat(key, def)
    fun getInt(key: String, def: Int) = prefs.getInt(key, def)
    fun getString(key: String, def: String? = null) = prefs.getString(key, def)
    fun getStringSet(key: String, def: Set<String>) = prefs.getStringSet(key, def)

    fun remove(key: String) = prefs.edit().remove(key).apply()

    fun putBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    fun putFloat(key: String, value: Float) = prefs.edit().putFloat(key, value).apply()
    fun putInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
    fun putString(key: String, value: String?) = prefs.edit().putString(key, value).apply()
    fun putStringSet(key: String, set: Set<String>) = prefs.edit().putStringSet(key, set).apply()
}