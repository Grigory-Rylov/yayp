package com.grishberg.yayp.domain

interface VideoListAction {
    fun hideAnimated() = Unit
    fun showAnimated() = Unit
    fun closeApp() = Unit

    object STUB : VideoListAction
}