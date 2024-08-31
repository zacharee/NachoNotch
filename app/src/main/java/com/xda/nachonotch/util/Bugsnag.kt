package com.xda.nachonotch.util

import android.util.Log
import com.bugsnag.android.BreadcrumbType
import com.bugsnag.android.Bugsnag
import com.xda.nachonotch.BuildConfig

object LoggingBugsnag {
    fun leaveBreadcrumb(
        message: String,
        metadata: Map<String, Any?>? = null,
        type: BreadcrumbType? = null,
        error: Throwable? = null
    ) {
        if (BuildConfig.DEBUG) {
            Log.e("NachoNotch", "$message, ${metadata}, $type")
        }

        val realMetadata = HashMap(metadata ?: mapOf())
        realMetadata["error"] = error?.stackTraceToString()

        Bugsnag.leaveBreadcrumb(message, realMetadata, type ?: BreadcrumbType.MANUAL)
    }
}
