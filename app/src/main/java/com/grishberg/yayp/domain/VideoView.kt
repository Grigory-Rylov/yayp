package com.grishberg.yayp.domain

interface VideoView {
    fun showAnimated() = Unit
    fun hideAnimated() = Unit
    fun play(videoId: String) = Unit
    fun stopPlay() = Unit
    fun seekTo(position: Int) = Unit
    fun position() = 0

    object STUB : VideoView
}