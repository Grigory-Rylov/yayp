package com.grishberg.backgroundyoutubeplayer

import android.view.SurfaceHolder
import android.widget.MediaController

interface Player {
    fun attachView(surfaceHolder: SurfaceHolder)
    fun detachView()
    fun stop()
    fun playVideo(id: String)
    fun setMediaController(mediaController: MediaControllerFacade)

    object STUB : Player {
        override fun attachView(surfaceHolder: SurfaceHolder) = Unit
        override fun detachView() = Unit
        override fun stop() = Unit
        override fun playVideo(id: String) = Unit
        override fun setMediaController(mediaController: MediaControllerFacade) = Unit
    }
}