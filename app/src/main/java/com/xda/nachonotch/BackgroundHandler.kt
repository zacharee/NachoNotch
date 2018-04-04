package com.xda.nachonotch

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.view.*

class BackgroundHandler : Service() {
    companion object {
        const val SHOULD_RUN = "enabled"
    }

    private lateinit var windowManager: WindowManager
    private lateinit var cover: Overlay
    private lateinit var orientationEventListener: OrientationEventListener

    private val params = WindowManager.LayoutParams()

    override fun onCreate() {
        super.onCreate()

        cover = Overlay(this)

        params.width = Utils.getRealScreenSize(this).x
        params.height = Utils.getStatusBarHeight(this)
        params.gravity = Gravity.TOP
        params.type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_PHONE
        else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN or
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        params.format = PixelFormat.TRANSLUCENT

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Utils.isEnabled(this)) addOverlayAndEnable()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlayAndDisable()
    }

    private fun addOverlayAndEnable() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            val notifMan = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifMan.createNotificationChannel(NotificationChannel("nachonotch", resources.getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW))
        }

        val notification = NotificationCompat.Builder(this, "nachonotch")
                .setContentTitle(resources.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_space_bar_black_24dp)
                .setPriority(Notification.PRIORITY_MIN)
                .build()

        startForeground(1, notification)
        orientationEventListener.enable()

        addOverlay()
    }

    private fun addOverlay() {
        removeOverlay()
        windowManager.addView(cover, params)

        if (cover.isImmersive()) hideOverlay()

        cover.setOnSystemUiVisibilityChangeListener {
            if (it and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR != 0) {
                cover.systemUiVisibility = cover.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }

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
    }

    private fun hideOverlay() {
        if (cover.visibility != View.GONE) {
            cover.visibility = View.GONE
            windowManager.updateViewLayout(cover, params)
        }
    }

    private fun showOverlay() {
        if (cover.visibility != View.VISIBLE) {
            cover.visibility = View.VISIBLE
            windowManager.updateViewLayout(cover, params)
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
}
