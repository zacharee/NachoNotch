package com.xda.nachonotch.util

import android.annotation.SuppressLint
import android.content.Context
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
        _environmentStatus.addAll(status)
        context.eventManager.sendEvent(Event.EnvironmentStatusUpdated)
    }

    fun removeStatus(vararg status: EnvironmentStatus) {
        _environmentStatus.removeAll(status.toSet())
        context.eventManager.sendEvent(Event.EnvironmentStatusUpdated)
    }
}