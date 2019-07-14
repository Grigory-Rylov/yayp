package com.example.backgroundplayer

import android.view.View

interface PlayerFacade {
    fun createView() : View

    fun showControls()

    fun playVideoById(id: String)

    fun stopPlaying()
}