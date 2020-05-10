package com.xda.nachonotch.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.provider.Settings
import android.view.IRotationWatcher
import android.view.Surface
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

    private val topCover by lazy { TopOverlay(this) }
    private val bottomCover by lazy { BottomOverlay(this) }
    private val topLeft by lazy { TopLeftCorner(this) }
    private val topRight by lazy { TopRightCorner(this) }
    private val bottomLeft by lazy { BottomLeftCorner(this) }
    private val bottomRight by lazy { BottomRightCorner(this) }
    private val immersiveManager by lazy {
        ImmersiveHelperManager(this).apply {
            immersiveListener = this@BackgroundHandler
        }
    }
    private val rotationWatcher by lazy {
        object : IRotationWatcher.Stub() {
            override fun onRotationChanged(rotation: Int) {
                immersiveManager.add()

                if (rotation == Surface.ROTATION_0) {
                    showAllOverlays()
                } else {
                    hideAllOverlays()
                }
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()

        prefManager.registerOnSharedPreferenceChangeListener(this)
        app.rotationWatchers.add(rotationWatcher)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (prefManager.isEnabled) addOverlayAndEnable()
        else stopSelf()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        removeAllOverlays()
        immersiveManager.remove()

        prefManager.unregisterOnSharedPreferenceChangeListener(this)
        app.rotationWatchers.remove(rotationWatcher)
    }

    override fun onImmersiveChange() {
        if (cachedRotation == Surface.ROTATION_0) {
            val status = immersiveManager.isStatusImmersive()

            if (status) {
                hideTop()
            } else {
                showTop()
            }

            immersiveManager.isNavImmersive {
                if (it) {
                    hideBottom()
                } else {
                    showBottom()
                }
            }
        } else {
            hideAllOverlays()
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
                    if (prefManager.coverNav) {
                        bottomCover.update(wm)
                    }
                    if (prefManager.useBottomCorners) {
                        bottomLeft.update(wm)
                        bottomRight.update(wm)
                    }
                }
                PrefManager.STATUS_HEIGHT -> {
                    topCover.update(wm)
                    if (prefManager.useTopCorners) {
                        topLeft.update(wm)
                        topRight.update(wm)
                    }
                }
                PrefManager.TOP_CORNER_WIDTH,
                PrefManager.TOP_CORNER_HEIGHT -> {
                    if (prefManager.useTopCorners) {
                        topLeft.update(wm)
                        topRight.update(wm)
                    }
                }
                PrefManager.BOTTOM_CORNER_WIDTH,
                PrefManager.BOTTOM_CORNER_HEIGHT -> {
                    if (prefManager.useBottomCorners) {
                        bottomLeft.update(wm)
                        bottomRight.update(wm)
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
        immersiveManager.add()

        addAllOverlays()
    }

    private fun addAllOverlays() {
        if (Settings.canDrawOverlays(this)) {
            if (cachedRotation == Surface.ROTATION_0) {
                removeAllOverlays()

                addTopOverlay()
                addTopCorners()
                addBottomOverlay()
                addBottomCorners()
            }
        } else {
            launchOverlaySettings()
        }
    }

    fun removeAllOverlays() {
        removeTopOverlay()
        removeTopCorners()
        removeBottomOverlay()
        removeBottomCorners()
    }
    
    private fun showAllOverlays() {
        showTop()
        showBottom()
    }

    private fun showTop() {
        topCover.show(wm)
        topLeft.show(wm)
        topRight.show(wm)
    }

    private fun showBottom() {
        bottomCover.show(wm)
        bottomLeft.show(wm)
        bottomRight.show(wm)
    }
    
    private fun hideAllOverlays() {
        hideTop()
        hideBottom()
    }

    private fun hideTop() {
        topCover.hide(wm)
        topLeft.hide(wm)
        topRight.hide(wm)
    }

    private fun hideBottom() {
        bottomCover.hide(wm)
        bottomLeft.hide(wm)
        bottomRight.hide(wm)
    }

    private fun addTopOverlay() {
        handler.postDelayed({
            topCover.add(wm)
        }, DELAY_MS)
    }

    private fun addBottomOverlay() {
        handler.postDelayed({
            if (prefManager.coverNav) {
                bottomCover.add(wm)
            }
        }, DELAY_MS)
    }

    private fun addTopCorners() {
        handler.postDelayed({
            if (prefManager.useTopCorners) {
                topRight.add(wm)
                topLeft.add(wm)
            }
        }, DELAY_MS)
    }

    private fun addBottomCorners() {
        handler.postDelayed({
            if (prefManager.useBottomCorners) {
                bottomRight.add(wm)
                bottomLeft.add(wm)
            }
        }, DELAY_MS)
    }

    private fun removeTopOverlay() {
        topCover.remove(wm)
    }

    private fun removeBottomOverlay() {
        bottomCover.remove(wm)
    }

    private fun removeTopCorners() {
        topLeft.remove(wm)
        topRight.remove(wm)
    }

    private fun removeBottomCorners() {
        bottomLeft.remove(wm)
        bottomRight.remove(wm)
    }

    override fun onBind(intent: Intent): IBinder? {
        return Binder()
    }
}
