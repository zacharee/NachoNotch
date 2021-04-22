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

    enum class BarPosition {
        TOP,
        BOTTOM
    }

    enum class CornerPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    private val bars = HashMap<BarPosition, BaseOverlay>()
    private val corners = HashMap<CornerPosition, BaseOverlay>()

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
                    removeEnvironmentStatus(BaseOverlay.EnvironmentStatus.LANDSCAPE)
                } else {
                    addEnvironmentStatus(BaseOverlay.EnvironmentStatus.LANDSCAPE)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        bars[BarPosition.TOP] = TopOverlay(this)
        bars[BarPosition.BOTTOM] = BottomOverlay(this)

        corners[CornerPosition.TOP_LEFT] = TopLeftCorner(this)
        corners[CornerPosition.TOP_RIGHT] = TopRightCorner(this)
        corners[CornerPosition.BOTTOM_LEFT] = BottomLeftCorner(this)
        corners[CornerPosition.BOTTOM_RIGHT] = BottomRightCorner(this)

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
        val status = immersiveManager.isStatusImmersive()

        if (status) {
            addEnvironmentStatus(BaseOverlay.EnvironmentStatus.STATUS_IMMERSIVE)
        } else {
            removeEnvironmentStatus(BaseOverlay.EnvironmentStatus.STATUS_IMMERSIVE)
        }

        immersiveManager.isNavImmersive { nav ->
            if (nav) {
                addEnvironmentStatus(BaseOverlay.EnvironmentStatus.NAV_IMMERSIVE)
            } else {
                removeEnvironmentStatus(BaseOverlay.EnvironmentStatus.NAV_IMMERSIVE)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (prefManager.isEnabled) {
            when (key) {
                PrefManager.ROUNDED_CORNERS_TOP -> {
                    removeCorners(CornerPosition.TOP_LEFT, CornerPosition.TOP_RIGHT)
                    addCorners(CornerPosition.TOP_LEFT, CornerPosition.TOP_RIGHT)
                }
                PrefManager.ROUNDED_CORNERS_BOTTOM -> {
                    removeCorners(CornerPosition.BOTTOM_LEFT, CornerPosition.BOTTOM_RIGHT)
                    addCorners(CornerPosition.BOTTOM_LEFT, CornerPosition.BOTTOM_RIGHT)
                }
                PrefManager.COVER_NAV -> {
                    removeBars(BarPosition.BOTTOM)
                    addBars(BarPosition.BOTTOM)
                }
                PrefManager.NAV_HEIGHT -> {
                    if (prefManager.coverNav) {
                        updateBars(BarPosition.BOTTOM)
                    }
                    if (prefManager.useBottomCorners) {
                        updateCorners(CornerPosition.BOTTOM_LEFT, CornerPosition.BOTTOM_RIGHT)
                    }
                }
                PrefManager.STATUS_HEIGHT -> {
                    updateBars(BarPosition.TOP)
                    if (prefManager.useTopCorners) {
                        updateCorners(CornerPosition.TOP_LEFT, CornerPosition.TOP_RIGHT)
                    }
                }
                PrefManager.TOP_CORNER_WIDTH,
                PrefManager.TOP_CORNER_HEIGHT -> {
                    if (prefManager.useTopCorners) {
                        updateCorners(CornerPosition.TOP_LEFT, CornerPosition.TOP_RIGHT)
                    }
                }
                PrefManager.BOTTOM_CORNER_WIDTH,
                PrefManager.BOTTOM_CORNER_HEIGHT -> {
                    if (prefManager.useBottomCorners) {
                        updateCorners(CornerPosition.BOTTOM_LEFT, CornerPosition.BOTTOM_RIGHT)
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

                addBars(*bars.keys.toTypedArray())
                addCorners(*corners.keys.toTypedArray())
            }
        } else {
            launchOverlaySettings()
        }
    }

    private fun removeAllOverlays() {
        removeBars(*bars.keys.toTypedArray())
        removeCorners(*corners.keys.toTypedArray())
    }

    private fun addBars(vararg bars: BarPosition) {
        bars.forEach {
            this.bars[it]?.add(wm)
        }
    }

    private fun updateBars(vararg bars: BarPosition) {
        bars.forEach {
            this.bars[it]?.update(wm)
        }
    }

    private fun removeBars(vararg bars: BarPosition) {
        bars.forEach {
            this.bars[it]?.remove(wm)
        }
    }

    private fun addCorners(vararg corners: CornerPosition) {
        corners.forEach {
            this.corners[it]?.add(wm)
        }
    }

    private fun updateCorners(vararg corners: CornerPosition) {
        corners.forEach {
            this.corners[it]?.update(wm)
        }
    }

    private fun removeCorners(vararg corners: CornerPosition) {
        corners.forEach {
            this.corners[it]?.remove(wm)
        }
    }

    private fun addEnvironmentStatus(vararg status: BaseOverlay.EnvironmentStatus) {
        bars.values.forEach { it.addStatus(wm, *status) }
        corners.values.forEach { it.addStatus(wm, *status) }
    }

    private fun removeEnvironmentStatus(vararg status: BaseOverlay.EnvironmentStatus) {
        bars.values.forEach { it.removeStatus(wm, *status) }
        corners.values.forEach { it.removeStatus(wm, *status) }
    }

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }
}
