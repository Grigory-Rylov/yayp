package com.grishberg.backgroundyoutubeplayer

interface PlayerScreen {
    fun updateScreenSize(w: Int, h: Int)

    object STUB : PlayerScreen {
        override fun updateScreenSize(w: Int, h: Int) = Unit
    }
}