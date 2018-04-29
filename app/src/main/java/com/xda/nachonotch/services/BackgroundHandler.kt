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
import com.xda.nachonotch.views.LeftCorner
import com.xda.nachonotch.views.Overlay
import com.xda.nachonotch.views.RightCorner

class BackgroundHandler : Service(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        const val SHOULD_RUN = "enabled"
    }

    private lateinit var windowManager: WindowManager
    private lateinit var cover: Overlay
    private lateinit var left: LeftCorner
    private lateinit var right: RightCorner
    private lateinit var orientationEventListener: OrientationEventListener
    private lateinit var immersiveListener: ImmersiveListener
    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        cover = Overlay(this)
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
        removeOverlay()
        windowManager.addView(cover, cover.getParams())

        if (Utils.areCornersEnabled(this)) {
            windowManager.addView(left, left.getParams())
            windowManager.addView(right, right.getParams())
        }

        if (cover.isImmersive()) hideOverlay()

        cover.setOnSystemUiVisibilityChangeListener {
            if (cover.isImmersive(it)) hideOverlay() else showOverlay()
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
            windowManager.removeView(cover)
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
    }

    private fun hideOverlay() {
        if (cover.visibility != View.GONE) {
            cover.visibility = View.GONE
            windowManager.updateViewLayout(cover, cover.getParams())
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

    private fun showOverlay() {
        if (cover.visibility != View.VISIBLE) {
            cover.visibility = View.VISIBLE
            windowManager.updateViewLayout(cover, cover.getParams())
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
            if (cover.isImmersive()) {
                hideOverlay()
            } else {
                showOverlay()
            }
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            if (uri == Settings.Global.getUriFor(Settings.Global.POLICY_CONTROL)) {
                val current = Settings.Global.getString(contentResolver, Settings.Global.POLICY_CONTROL)

                if (current != null && current.isNotEmpty() && (current.contains("full"))) {
                    hideOverlay()
                } else {
                    showOverlay()
                }
            }
        }

        fun destroy() {
            contentResolver.unregisterContentObserver(this)
        }
    }
}
