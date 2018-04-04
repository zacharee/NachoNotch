package com.xda.nachonotch

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout

class Overlay : LinearLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        setBackgroundColor(Color.BLACK)
    }

    fun isImmersive(): Boolean {
        return isImmersive(systemUiVisibility)
    }

    fun isImmersive(vis: Int): Boolean {
        return vis and 6 == 4 || vis and 4 == 4
    }
}