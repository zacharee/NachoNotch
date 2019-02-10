package com.xda.nachonotch.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.xda.nachonotch.R
import com.xda.nachonotch.activities.SettingsActivity
import com.xda.nachonotch.util.*
import com.xda.nachonotch.views.*

class BackgroundHandler : Service(), SharedPreferences.OnSharedPreferenceChangeListener, ImmersiveChangeListener {
    companion object {
        const val SHOULD_RUN = "enabled"
        const val DELAY_MS = 200L
    }

    private val windowManager by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }

    private val topCover by lazy { TopOverlay(this) }
    private val bottomCover by lazy { BottomOverlay(this) }
    private val topLeft by lazy { TopLeftCorner(this) }
    private val topRight by lazy { TopRightCorner(this) }
    private val bottomLeft by lazy { BottomLeftCorner(this) }
    private val bottomRight by lazy { BottomRightCorner(this) }
    private val immersiveManager by lazy {
        ImmersiveHelperManager(this).apply {
            changeListener = this@BackgroundHandler
        }
    }

    private val handler = Handler(Looper.getMainLooper())

    private val orientationEventListener by lazy {
        object : OrientationEventListener(this) {
            var oldRot = Surface.ROTATION_0
            override fun onOrientationChanged(i: Int) {
                val currentRot = windowManager.defaultDisplay.rotation
                if (oldRot != currentRot) {
                    if (currentRot == Surface.ROTATION_0) {
                        addAllOverlays()
                    } else removeAllOverlays()
                    oldRot = currentRot
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        prefManager.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (prefManager.isEnabled) addOverlayAndEnable()
        else removeOverlayAndDisable()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlayAndDisable()
        immersiveManager.remove()

        prefManager.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onImmersiveChange() {
        val status = immersiveManager.isStatusImmersive()
        val nav = immersiveManager.isNavImmersive()

        Log.e("NachoNotch", "status: $status, nav: $nav")

        if (status) {
            removeTopOverlay()
            removeTopCorners()
        } else {
            addTopOverlay()
            addTopCorners()
        }

        if (nav) {
            removeBottomOverlay()
            removeBottomCorners()
        } else {
            addTopOverlay()
            addTopCorners()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (prefManager.isEnabled) {
            when (key) {
                PrefManager.ROUNDED_CORNERS_TOP -> {
                    if (prefManager.useTopCorners) {
                        addTopCorners()
                    } else removeTopCorners()
                }
                PrefManager.ROUNDED_CORNERS_BOTTOM -> {
                    if (prefManager.useBottomCorners) {
                        addBottomCorners()
                    } else removeBottomCorners()
                }
                PrefManager.COVER_NAV -> {
                    if (prefManager.coverNav) {
                        addBottomOverlay()
                    } else removeBottomOverlay()
                }
                PrefManager.NAV_HEIGHT -> {
                    if (prefManager.coverNav) try {
                        windowManager.updateViewLayout(bottomCover, bottomCover.getParams())
                    } catch (e: Exception) {
                    }
                    if (prefManager.useBottomCorners) try {
                        windowManager.updateViewLayout(bottomLeft, bottomLeft.getParams())
                        windowManager.updateViewLayout(bottomRight, bottomRight.getParams())
                    } catch (e: Exception) {
                    }
                }
                PrefManager.STATUS_HEIGHT -> {
                    try {
                        windowManager.updateViewLayout(topCover, topCover.getParams())
                    } catch (e: Exception) {
                    }
                    if (prefManager.useTopCorners) try {
                        windowManager.updateViewLayout(topLeft, topLeft.getParams())
                        windowManager.updateViewLayout(topRight, topRight.getParams())
                    } catch (e: Exception) {
                    }
                }
                PrefManager.TOP_CORNER_WIDTH,
                PrefManager.TOP_CORNER_HEIGHT -> {
                    if (prefManager.useTopCorners) try {
                        windowManager.updateViewLayout(topLeft, topLeft.getParams())
                        windowManager.updateViewLayout(topRight, topRight.getParams())
                    } catch (e: Exception) {
                    }
                }
                PrefManager.BOTTOM_CORNER_WIDTH,
                PrefManager.BOTTOM_CORNER_HEIGHT -> {
                    if (prefManager.useBottomCorners) try {
                        windowManager.updateViewLayout(bottomLeft, bottomLeft.getParams())
                        windowManager.updateViewLayout(bottomRight, bottomRight.getParams())
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    private fun addOverlayAndEnable() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            val notifMan = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifMan.createNotificationChannel(NotificationChannel("nachonotch", resources.getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW))
        }

        val notification = NotificationCompat.Builder(this, "nachonotch")
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText(resources.getString(R.string.settings_prompt))
                .setContentIntent(PendingIntent.getActivity(this, 100,
                        Intent(this, SettingsActivity::class.java), 0))
                .setSmallIcon(R.drawable.ic_space_bar_black_24dp)
                .setPriority(if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                    Notification.PRIORITY_MIN else Notification.PRIORITY_LOW)
                .build()

        startForeground(1, notification)
        orientationEventListener.enable()
        immersiveManager.add()

        addAllOverlays()
    }

    private fun removeOverlayAndDisable() {
        stopForeground(true)
        removeAllOverlays()
        orientationEventListener.disable()
        immersiveManager.remove()
        stopSelf()
    }

    private fun addAllOverlays() {
        if (Settings.canDrawOverlays(this)) {
            removeAllOverlays()

            addTopOverlay()
            addTopCorners()
            addBottomOverlay()
            addBottomCorners()
        } else {
            launchOverlaySettings()
        }
    }

    private fun removeAllOverlays() {
        removeTopOverlay()
        removeTopCorners()
        removeBottomOverlay()
        removeBottomCorners()
    }

    private fun addTopOverlay() {
        handler.postDelayed({
            try {
                windowManager.addView(topCover, topCover.getParams())
            } catch (e: Exception) {}
        }, DELAY_MS)
    }

    private fun addBottomOverlay() {
        handler.postDelayed({
            if (prefManager.coverNav) {
                try {
                    windowManager.addView(bottomCover, bottomCover.getParams())
                } catch (e: Exception) {}
            }
        }, DELAY_MS)
    }

    private fun addTopCorners() {
        handler.postDelayed({
            if (prefManager.useTopCorners) {
                try {
                    windowManager.addView(topRight, topRight.getParams())
                } catch (e: Exception) {}

                try {
                    windowManager.addView(topLeft, topLeft.getParams())
                } catch (e: Exception) {}
            }
        }, DELAY_MS)
    }

    private fun addBottomCorners() {
        handler.postDelayed({
            if (prefManager.useBottomCorners) {
                try {
                    windowManager.addView(bottomRight, bottomRight.getParams())
                } catch (e: Exception) {}

                try {
                    windowManager.addView(bottomLeft, bottomLeft.getParams())
                } catch (e: Exception) {}
            }
        }, DELAY_MS)
    }

    private fun removeTopOverlay() {
        try {
            windowManager.removeView(topCover)
        } catch (e: Exception) {}
    }

    private fun removeBottomOverlay() {
        try {
            windowManager.removeView(bottomCover)
        } catch (e: Exception) {}
    }

    private fun removeTopCorners() {
        try {
            windowManager.removeView(topRight)
        } catch (e: Exception) {}

        try {
            windowManager.removeView(topLeft)
        } catch (e: Exception) {}
    }

    private fun removeBottomCorners() {
        try {
            windowManager.removeView(bottomRight)
        } catch (e: Exception) {}

        try {
            windowManager.removeView(bottomLeft)
        } catch (e: Exception) {}
    }

    override fun onBind(intent: Intent): IBinder? {
        return Binder()
    }
}
