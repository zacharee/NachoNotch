package com.xda.nachonotch.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.widget.Toast
import com.xda.nachonotch.R
import com.xda.nachonotch.activities.TermsActivity

val Context.hasNavBar: Boolean
    get() {
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && resources.getBoolean(id)
                || Build.MODEL.contains("Android SDK built for x86")
    }

val Context.prefManager: PrefManager
    get() = PrefManager.getInstance(this)

val Context.resourceNavBarHeight: Int
    get() = if (hasNavBar)
        resources.getDimensionPixelSize(
                resources.getIdentifier("navigation_bar_height", "dimen", "android")) else 0

val Context.resourceStatusBarHeight: Int
    get() = resources.getDimensionPixelSize(
            resources.getIdentifier("status_bar_height", "dimen", "android"))

fun Context.dpAsPx(dpVal: Number) =
        Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal.toFloat(), resources.displayMetrics))

fun Context.enforceTerms(): Boolean {
    return if (prefManager.termsVersion < Utils.TERMS_VERSION) {
        startActivity(
                Intent(this, TermsActivity::class.java)
                        .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        false
    } else true
}

fun Context.launchOverlaySettings() {
    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    try {
        startActivity(intent)
    } catch (e: Exception) {
        intent.data = null

        startActivity(intent)
    }

    Toast.makeText(this, R.string.enable_overlay_permission, Toast.LENGTH_SHORT).show()
}

object Utils {
    val IMMERSIVE_STATUS = 4
    val IMMERSIVE_NAV = 2
    val IMMERSIVE_FULL = IMMERSIVE_STATUS or IMMERSIVE_NAV

    val TERMS_VERSION = 1
}