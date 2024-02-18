package com.xda.nachonotch.util

import android.annotation.SuppressLint
import android.content.Context
import com.bugsnag.android.Bugsnag
import java.util.concurrent.ConcurrentHashMap

val Context.environmentManager: EnvironmentManager
    get() = EnvironmentManager.getInstance(this)

class EnvironmentManager private constructor(private val context: Context) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: EnvironmentManager? = null

        fun getInstance(context: Context) : EnvironmentManager {
            return instance ?: EnvironmentManager(context.applicationContext ?: context).apply {
                instance = this
            }
        }
    }

    enum class EnvironmentStatus {
        STATUS_IMMERSIVE,
        NAV_IMMERSIVE,
        LANDSCAPE
    }

    private val _environmentStatus: ConcurrentHashMap.KeySetView<EnvironmentStatus, Boolean> = ConcurrentHashMap.newKeySet()
    val environmentStatus: Set<EnvironmentStatus>
        get() = _environmentStatus

    fun addStatus(vararg status: EnvironmentStatus) {
        Bugsnag.leaveBreadcrumb("Adding EnvironmentStatus ${status.contentToString()}")
        _environmentStatus.addAll(status.toSet())
        context.eventManager.sendEvent(Event.EnvironmentStatusUpdated)
    }

    fun removeStatus(vararg status: EnvironmentStatus) {
        Bugsnag.leaveBreadcrumb("Removing EnvironmentStatus ${status.contentToString()}")
        _environmentStatus.removeAll(status.toSet())
        context.eventManager.sendEvent(Event.EnvironmentStatusUpdated)
    }
}