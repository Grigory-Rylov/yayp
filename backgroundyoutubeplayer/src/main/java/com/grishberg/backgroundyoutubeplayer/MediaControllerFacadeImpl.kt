package com.grishberg.backgroundyoutubeplayer

import android.widget.MediaController

class MediaControllerFacadeImpl(
    private val mediaController: MediaController
) : MediaControllerFacade {

    override fun setMediaPlayer(mc: MediaController.MediaPlayerControl) {
        mediaController.setMediaPlayer(mc)
    }

    override fun show() {
        mediaController.show()
    }
}