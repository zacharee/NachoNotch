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
    ) {
        if (BuildConfig.DEBUG) {
            Log.e("NachoNotch", "$message, ${metadata}, $type")
        }

        when {
            metadata == null && type != null -> Bugsnag.leaveBreadcrumb(message, mapOf(), type)
            metadata != null && type != null -> Bugsnag.leaveBreadcrumb(message, metadata, type)
            metadata != null && type == null -> Bugsnag.leaveBreadcrumb(message, metadata, BreadcrumbType.MANUAL)
            else -> Bugsnag.leaveBreadcrumb(message)
        }
    }
}
