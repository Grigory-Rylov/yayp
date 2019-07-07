package com.grishberg.yayp.domain

interface VideoListAction {
    fun hideAnimated() = STUB
    fun showAnimated() = STUB

    object STUB : VideoListAction
}