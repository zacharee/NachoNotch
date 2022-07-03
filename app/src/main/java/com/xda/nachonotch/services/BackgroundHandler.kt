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
    }

    enum class OverlayPosition {
        TOP_BAR,
        BOTTOM_BAR,
        TOP_LEFT_CORNER,
        TOP_RIGHT_CORNER,
        BOTTOM_LEFT_CORNER,
        BOTTOM_RIGHT_CORNER
    }

    private val overlays = HashMap<OverlayPosition, BaseOverlay>()

    private val immersiveManager by lazy {
        ImmersiveHelperManager(this, this)
    }
    private val rotationWatcher by lazy {
        object : IRotationWatcher.Stub() {
            override fun onRotationChanged(rotation: Int) {
                immersiveManager.add()

                if (rotation == Surface.ROTATION_0) {
                    environmentManager.removeStatus(EnvironmentManager.EnvironmentStatus.LANDSCAPE)
                } else {
                    environmentManager.addStatus(EnvironmentManager.EnvironmentStatus.LANDSCAPE)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        immersiveManager.onCreate()

        overlays[OverlayPosition.TOP_BAR] = TopOverlay(this)
        overlays[OverlayPosition.BOTTOM_BAR] = BottomOverlay(this)

        overlays[OverlayPosition.TOP_LEFT_CORNER] = TopLeftCorner(this)
        overlays[OverlayPosition.TOP_RIGHT_CORNER] = TopRightCorner(this)
        overlays[OverlayPosition.BOTTOM_LEFT_CORNER] = BottomLeftCorner(this)
        overlays[OverlayPosition.BOTTOM_RIGHT_CORNER] = BottomRightCorner(this)

        prefManager.registerOnSharedPreferenceChangeListener(this)
        app.rotationWatchers.add(rotationWatcher)

        overlays.values.forEach { it.onCreate() }
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
        immersiveManager.onDestroy()

        prefManager.unregisterOnSharedPreferenceChangeListener(this)
        app.rotationWatchers.remove(rotationWatcher)

        overlays.values.forEach { it.onDestroy() }
    }

    override fun onImmersiveChange() {
        val status = immersiveManager.isStatusImmersive()

        if (status) {
            environmentManager.addStatus(EnvironmentManager.EnvironmentStatus.STATUS_IMMERSIVE)
        } else {
            environmentManager.removeStatus(EnvironmentManager.EnvironmentStatus.STATUS_IMMERSIVE)
        }

        immersiveManager.isNavImmersive { nav ->
            if (nav) {
                environmentManager.addStatus(EnvironmentManager.EnvironmentStatus.NAV_IMMERSIVE)
            } else {
                environmentManager.removeStatus(EnvironmentManager.EnvironmentStatus.NAV_IMMERSIVE)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (prefManager.isEnabled) {
            when (key) {
                PrefManager.ROUNDED_CORNERS_TOP -> {
                    removeOverlays(OverlayPosition.TOP_LEFT_CORNER, OverlayPosition.TOP_RIGHT_CORNER)
                    addOverlays(OverlayPosition.TOP_LEFT_CORNER, OverlayPosition.TOP_RIGHT_CORNER)
                }
                PrefManager.ROUNDED_CORNERS_BOTTOM -> {
                    removeOverlays(OverlayPosition.BOTTOM_LEFT_CORNER, OverlayPosition.BOTTOM_RIGHT_CORNER)
                    addOverlays(OverlayPosition.BOTTOM_LEFT_CORNER, OverlayPosition.BOTTOM_RIGHT_CORNER)
                }
                PrefManager.COVER_NAV -> {
                    removeOverlays(OverlayPosition.BOTTOM_BAR)
                    addOverlays(OverlayPosition.BOTTOM_BAR)
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
                        Intent(this, SettingsActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
                .setSmallIcon(R.drawable.ic_space_bar_black_24dp)
                .setPriority(if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                    NotificationCompat.PRIORITY_MIN else NotificationCompat.PRIORITY_LOW)
                .build()

        startForeground(1, notification)
        immersiveManager.add()

        addAllOverlays()
    }

    private fun addAllOverlays() {
        if (Settings.canDrawOverlays(this)) {
            if (cachedRotation == Surface.ROTATION_0) {
                removeAllOverlays()

                addOverlays(*overlays.keys.toTypedArray())
            }
        } else {
            launchOverlaySettings()
        }
    }

    private fun removeAllOverlays() {
        removeOverlays(*overlays.keys.toTypedArray())
    }

    private fun addOverlays(vararg overlays: OverlayPosition) {
        overlays.forEach {
            this.overlays[it]?.add()
        }
    }

    private fun removeOverlays(vararg overlays: OverlayPosition) {
        overlays.forEach {
            this.overlays[it]?.remove()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }
}
