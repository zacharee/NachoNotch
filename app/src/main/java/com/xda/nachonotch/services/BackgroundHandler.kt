package com.xda.nachonotch.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.ContentObserver
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.xda.nachonotch.R
import com.xda.nachonotch.activities.SettingsActivity
import com.xda.nachonotch.util.PrefManager
import com.xda.nachonotch.util.launchOverlaySettings
import com.xda.nachonotch.util.prefManager
import com.xda.nachonotch.views.*

class BackgroundHandler : Service(), SharedPreferences.OnSharedPreferenceChangeListener {
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
    private val immersiveListener by lazy { ImmersiveListener() }

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
        immersiveListener.destroy()

        prefManager.unregisterOnSharedPreferenceChangeListener(this)
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
                .setContentIntent(PendingIntent.getActivity(this, 100, Intent(this, SettingsActivity::class.java), 0))
                .setSmallIcon(R.drawable.ic_space_bar_black_24dp)
                .setPriority(if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) Notification.PRIORITY_MIN else Notification.PRIORITY_LOW)
                .build()

        startForeground(1, notification)
        orientationEventListener.enable()

        addAllOverlays()
    }

    private fun removeOverlayAndDisable() {
        stopForeground(true)
        removeAllOverlays()
        orientationEventListener.disable()
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
                topCover.setOnSystemUiVisibilityChangeListener(immersiveListener)
            } catch (e: Exception) {
            }
        }, DELAY_MS)
    }

    private fun addBottomOverlay() {
        handler.postDelayed({
            if (prefManager.coverNav) {
                try {
                    windowManager.addView(bottomCover, bottomCover.getParams())
                } catch (e: Exception) {
                }
            }
        }, DELAY_MS)
    }

    private fun addTopCorners() {
        handler.postDelayed({
            if (prefManager.useTopCorners) {
                try {
                    windowManager.addView(topRight, topRight.getParams())
                } catch (e: Exception) {
                }

                try {
                    windowManager.addView(topLeft, topLeft.getParams())
                } catch (e: Exception) {
                }
            }
        }, DELAY_MS)
    }

    private fun addBottomCorners() {
        handler.postDelayed({
            if (prefManager.useBottomCorners) {
                try {
                    windowManager.addView(bottomRight, bottomRight.getParams())
                } catch (e: Exception) {
                }

                try {
                    windowManager.addView(bottomLeft, bottomLeft.getParams())
                } catch (e: Exception) {
                }
            }
        }, DELAY_MS)
    }

    private fun removeTopOverlay() {
        try {
            windowManager.removeView(topCover)
        } catch (e: Exception) {
        }
    }

    private fun removeBottomOverlay() {
        try {
            windowManager.removeView(bottomCover)
        } catch (e: Exception) {
        }
    }

    private fun removeTopCorners() {
        try {
            windowManager.removeView(topRight)
        } catch (e: Exception) {
        }

        try {
            windowManager.removeView(topLeft)
        } catch (e: Exception) {
        }
    }

    private fun removeBottomCorners() {
        try {
            windowManager.removeView(bottomRight)
        } catch (e: Exception) {
        }

        try {
            windowManager.removeView(bottomLeft)
        } catch (e: Exception) {
        }
    }

    private fun hideTopOverlay() {
        if (topCover.visibility != View.GONE) {
            topCover.visibility = View.GONE
            try {
                windowManager.updateViewLayout(topCover, topCover.getParams())
            } catch (e: Exception) {
            }
        }

        if (prefManager.useTopCorners) {
            if (topLeft.visibility != View.GONE) {
                topLeft.visibility = View.GONE
                try {
                    windowManager.updateViewLayout(topLeft, topLeft.getParams())
                } catch (e: Exception) {
                }
            }

            if (topRight.visibility != View.GONE) {
                topRight.visibility = View.GONE
                try {
                    windowManager.updateViewLayout(topRight, topRight.getParams())
                } catch (e: Exception) {
                }
            }
        }
    }

    private fun showTopOverlay() {
        if (!topCover.isHidden()) {
            if (topCover.visibility != View.VISIBLE) {
                topCover.visibility = View.VISIBLE
                try {
                    windowManager.updateViewLayout(topCover, topCover.getParams())
                } catch (e: Exception) {
                }
            }

            if (prefManager.useTopCorners) {
                if (topLeft.visibility != View.VISIBLE) {
                    topLeft.visibility = View.VISIBLE
                    try {
                        windowManager.updateViewLayout(topLeft, topLeft.getParams())
                    } catch (e: Exception) {
                    }
                }

                if (topRight.visibility != View.VISIBLE) {
                    topRight.visibility = View.VISIBLE
                    try {
                        windowManager.updateViewLayout(topRight, topRight.getParams())
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    private fun hideBottomOverlay() {
        if (prefManager.coverNav) {
            if (bottomCover.visibility != View.GONE) {
                bottomCover.visibility = View.GONE
                try {
                    windowManager.updateViewLayout(bottomCover, bottomCover.getParams())
                } catch (e: Exception) {
                }
            }

            if (prefManager.useBottomCorners) {
                if (bottomLeft.visibility != View.GONE) {
                    bottomLeft.visibility = View.GONE
                    try {
                        windowManager.updateViewLayout(bottomLeft, bottomLeft.getParams())
                    } catch (e: Exception) {
                    }
                }

                if (bottomRight.visibility != View.GONE) {
                    bottomRight.visibility = View.GONE
                    try {
                        windowManager.updateViewLayout(bottomRight, bottomRight.getParams())
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    private fun showBottomOverlay() {
        if (prefManager.coverNav || !bottomCover.isHidden()) {
            if (bottomCover.visibility != View.VISIBLE) {
                bottomCover.visibility = View.VISIBLE
                try {
                    windowManager.updateViewLayout(bottomCover, bottomCover.getParams())
                } catch (e: Exception) {
                }
            }

            if (prefManager.useBottomCorners) {
                if (bottomLeft.visibility != View.VISIBLE) {
                    bottomLeft.visibility = View.VISIBLE
                    try {
                        windowManager.updateViewLayout(bottomLeft, bottomLeft.getParams())
                    } catch (e: Exception) {
                    }
                }

                if (bottomRight.visibility != View.VISIBLE) {
                    bottomRight.visibility = View.VISIBLE
                    try {
                        windowManager.updateViewLayout(bottomRight, bottomRight.getParams())
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return Binder()
    }

    inner class ImmersiveListener : ContentObserver(handler), View.OnSystemUiVisibilityChangeListener {
        init {
            contentResolver.registerContentObserver(Settings.Global.CONTENT_URI, true, this)
        }

        override fun onSystemUiVisibilityChange(visibility: Int) {
            handle()
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            handle()
        }

        private fun handle() {
            if (topCover.isHidden()) hideTopOverlay() else showTopOverlay()
            if (bottomCover.isHidden()) hideBottomOverlay() else showBottomOverlay()
        }

        fun destroy() {
            contentResolver.unregisterContentObserver(this)
        }
    }
}
