package com.grishberg.yayp.player

import android.view.SurfaceHolder
import android.widget.MediaController

interface Player {
    val mediaController: MediaController.MediaPlayerControl
    fun attachView(surfaceHolder: SurfaceHolder)
    fun detachView()
    fun stop()
    fun playVideo(id: String)
}