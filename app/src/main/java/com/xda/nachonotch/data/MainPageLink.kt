package com.xda.nachonotch.data

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class MainPageLink(
    @DrawableRes
    val icon: Int,
    @StringRes
    val title: Int,
    @StringRes
    val desc: Int,
    val link: String = "",
    val isEmail: Boolean = false,
    val onClick: (Context.() -> Unit)? = null,
)