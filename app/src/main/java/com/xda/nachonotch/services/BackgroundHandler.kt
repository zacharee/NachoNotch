package com.xda.nachonotch.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.ContentObserver
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.view.WindowManager
import com.xda.nachonotch.R
import com.xda.nachonotch.activities.SettingsActivity
import com.xda.nachonotch.util.Utils
import com.xda.nachonotch.views.BottomOverlay
import com.xda.nachonotch.views.LeftCorner
import com.xda.nachonotch.views.RightCorner
import com.xda.nachonotch.views.TopOverlay

class BackgroundHandler : Service(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        const val SHOULD_RUN = "enabled"
    }

    private lateinit var windowManager: WindowManager
    private lateinit var topCover: TopOverlay
    private lateinit var bottomCover: BottomOverlay
    private lateinit var left: LeftCorner
    private lateinit var right: RightCorner
    private lateinit var orientationEventListener: OrientationEventListener
    private lateinit var immersiveListener: ImmersiveListener
    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        topCover = TopOverlay(this)
        bottomCover = BottomOverlay(this)
        left = LeftCorner(this)
        right = RightCorner(this)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        immersiveListener = ImmersiveListener()

        orientationEventListener = object : OrientationEventListener(this) {
            var oldRot = Surface.ROTATION_0
            override fun onOrientationChanged(i: Int) {
                val currentRot = windowManager.defaultDisplay.rotation
                if (oldRot != currentRot) {
                    if (currentRot == Surface.ROTATION_0) {
                        addOverlay()
                    }
                    else removeOverlay()
                    oldRot = currentRot
                }
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Utils.isEnabled(this)) addOverlayAndEnable()
        else removeOverlayAndDisable()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlayAndDisable()
        immersiveListener.destroy()

        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "rounded_corners" -> {
                if (Utils.areCornersEnabled(this)) {
                    windowManager.addView(left, left.getParams())
                    windowManager.addView(right, right.getParams())

                    if (topCover.isHidden()) hideTopOverlay()
                } else {
                    try {
                        windowManager.removeView(left)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        windowManager.removeView(right)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            "cover_nav" -> {
                if (Utils.isNavCoverEnabled(this)) {
                    windowManager.addView(bottomCover, bottomCover.getParams())
                    if (bottomCover.isHidden()) hideBottomOverlay()
                } else {
                    try {
                        windowManager.removeView(bottomCover)
                    } catch (e: Exception) {
                        e.printStackTrace()
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

        addOverlay()
    }

    private fun addOverlay() {
        try {
            removeOverlay()
            windowManager.addView(topCover, topCover.getParams())

            if (Utils.areCornersEnabled(this)) {
                windowManager.addView(left, left.getParams())
                windowManager.addView(right, right.getParams())
            }

            if (Utils.isNavCoverEnabled(this)) {
                windowManager.addView(bottomCover, bottomCover.getParams())
            }

            if (topCover.isHidden()) hideTopOverlay()
            if (bottomCover.isHidden()) hideBottomOverlay()

            topCover.setOnSystemUiVisibilityChangeListener {
                if (topCover.isHidden(it)) hideTopOverlay() else showTopOverlay()
            }

            bottomCover.setOnSystemUiVisibilityChangeListener {
                if (bottomCover.isHidden(it)) hideBottomOverlay() else showBottomOverlay()
            }
        } catch (e: WindowManager.BadTokenException) {
            Utils.launchOverlaySettings(this)
        }
    }

    private fun removeOverlayAndDisable() {
        stopForeground(true)
        removeOverlay()
        orientationEventListener.disable()
        stopSelf()
    }

    private fun removeOverlay() {
        try {
            windowManager.removeView(topCover)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        try {
            windowManager.removeView(left)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        try {
            windowManager.removeView(right)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            windowManager.removeView(bottomCover)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideTopOverlay() {
        if (topCover.visibility != View.GONE) {
            topCover.visibility = View.GONE
            windowManager.updateViewLayout(topCover, topCover.getParams())
        }
        
        if (Utils.areCornersEnabled(this)) {
            if (left.visibility != View.GONE) {
                left.visibility = View.GONE
                windowManager.updateViewLayout(left, left.getParams())
            }

            if (right.visibility != View.GONE) {
                right.visibility = View.GONE
                windowManager.updateViewLayout(right, right.getParams())
            }
        }
    }

    private fun showTopOverlay() {
        if (!topCover.isHidden()) {
            try {
                if (topCover.visibility != View.VISIBLE) {
                    topCover.visibility = View.VISIBLE
                    windowManager.updateViewLayout(topCover, topCover.getParams())
                }

                if (Utils.areCornersEnabled(this)) {
                    if (left.visibility != View.VISIBLE) {
                        left.visibility = View.VISIBLE
                        windowManager.updateViewLayout(left, left.getParams())
                    }

                    if (right.visibility != View.VISIBLE) {
                        right.visibility = View.VISIBLE
                        windowManager.updateViewLayout(right, right.getParams())
                    }
                }
            } catch (e: IllegalArgumentException) {}
        }
    }

    private fun hideBottomOverlay() {
        if (Utils.isNavCoverEnabled(this)) {
            try {
                if (bottomCover.visibility != View.GONE) {
                    bottomCover.visibility = View.GONE
                    windowManager.updateViewLayout(bottomCover, bottomCover.getParams())
                }
            } catch (e: IllegalArgumentException) {}
        }
    }

    private fun showBottomOverlay() {
        if (Utils.isNavCoverEnabled(this) || !bottomCover.isHidden()) {
            try {
                if (bottomCover.visibility != View.VISIBLE) {
                    bottomCover.visibility = View.VISIBLE
                    windowManager.updateViewLayout(bottomCover, bottomCover.getParams())
                }
            } catch (e: IllegalArgumentException) {}
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return BackgroundBinder()
    }

    inner class BackgroundBinder : Binder() {
        fun getService(): BackgroundHandler {
            return this@BackgroundHandler
        }
    }

    inner class ImmersiveListener : ContentObserver(Handler(Looper.getMainLooper())), View.OnSystemUiVisibilityChangeListener {
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
