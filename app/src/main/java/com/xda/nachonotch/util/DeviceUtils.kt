package com.xda.nachonotch.util

import android.content.Context

val Context.isPixelUI: Boolean
    get() = packageManager.hasSystemFeature("com.google.android.feature.PIXEL_EXPERIENCE")
