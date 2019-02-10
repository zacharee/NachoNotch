package com.xda.nachonotch.util

interface ImmersiveTypeListener {
    fun onFullChange(isFull: Boolean)
    fun onStatusChange(isStatus: Boolean)
    fun onNavChange(isNav: Boolean)
}