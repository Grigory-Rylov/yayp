package com.example.backgroundplayer

import android.view.View

interface PlayerFacade {
    fun createView() : View

    fun playStream(id: String)
}