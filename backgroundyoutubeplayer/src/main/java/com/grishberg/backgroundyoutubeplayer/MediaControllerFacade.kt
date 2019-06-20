package com.grishberg.backgroundyoutubeplayer

import android.widget.MediaController

interface MediaControllerFacade {
    fun setMediaPlayer(mc: MediaController.MediaPlayerControl)
    fun show()
}