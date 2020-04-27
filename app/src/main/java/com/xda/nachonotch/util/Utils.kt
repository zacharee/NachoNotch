package com.xda.nachonotch.util

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.Display
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.xda.nachonotch.App
import com.xda.nachonotch.R
import com.xda.nachonotch.activities.TermsActivity
import com.xda.nachonotch.util.Utils.TERMS_VERSION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.PrintWriter
import java.io.StringWriter

val mainHandler = Handler(Looper.getMainLooper())

val mainScope = CoroutineScope(Dispatchers.Main)
val logicScope = CoroutineScope(Dispatchers.IO)

val Context.realScreenSize: Point
    get() = Point(cachedScreenSize ?: refreshScreenSize())

var cachedRotation = Integer.MIN_VALUE

val Context.rotation: Int
    get() {
        return wm.defaultDisplay.rotation.also { cachedRotation = it }
    }

private var cachedScreenSize: Point? = null

/**
 * Try not to call this from the main Thread
 */
fun Context.refreshScreenSize(): Point {
    val display = wm.defaultDisplay

    val temp = Point().apply { display.getRealSize(this) }
    cachedScreenSize = temp

    return cachedScreenSize!!
}

val isLandscape: Boolean
    get() = cachedRotation.run { this == Surface.ROTATION_90 || this == Surface.ROTATION_270 }

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

val Context.wm: WindowManager
    get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager

fun Context.dpAsPx(dpVal: Number) =
        Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal.toFloat(), resources.displayMetrics))

fun Context.enforceTerms(): Boolean {
    return if (prefManager.termsVersion < TERMS_VERSION) {
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

val Context.app: App
    get() = applicationContext as App

fun Throwable.logStack() {
    val writer = StringWriter()
    val printer = PrintWriter(writer)

    printStackTrace(printer)

    Log.e("NachoNotch", writer.toString())
}

fun Display.getOverscanInsets(out: Rect) {
    Display::class.java.getMethod("getOverscanInsets", Rect::class.java)
            .invoke(this, out)
}

fun View.getBoundsOnScreen(out: Rect) {
    View::class.java.getMethod("getBoundsOnScreen", Rect::class.java)
            .invoke(this, out)
}

object Utils {
    val IMMERSIVE_STATUS = 4
    val IMMERSIVE_NAV = 2
    val IMMERSIVE_FULL = IMMERSIVE_STATUS or IMMERSIVE_NAV

    val TERMS_VERSION = 1
}