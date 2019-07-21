package com.grishberg.yayp.domain

interface VideoListAction {
    fun hideAnimated() = Unit
    fun showAnimated() = Unit
    fun closeApp() = Unit
    fun searchVideos(searchText: CharSequence) = Unit
    fun hideKeyboard() = Unit

    object STUB : VideoListAction
}