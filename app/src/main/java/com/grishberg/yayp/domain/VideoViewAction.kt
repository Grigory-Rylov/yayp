package com.grishberg.yayp.domain

interface VideoViewAction {
    fun showAnimated() = Unit
    fun hideAnimated() = Unit
    fun play(videoId: String) = Unit
    fun stopPlay() = Unit

    object STUB : VideoViewAction
}