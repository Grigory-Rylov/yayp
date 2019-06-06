package com.grishberg.yayp.player

import android.view.SurfaceHolder
import android.widget.MediaController
import com.grishberg.yayp.presentation.MediaControllerStub

class PlayerStub : Player {
    override val mediaController: MediaController.MediaPlayerControl
        get() = MediaControllerStub

    override fun attachView(surfaceHolder: SurfaceHolder) = Unit

    override fun detachView() = Unit

    override fun stop() = Unit

    override fun playVideo(id: String) = Unit
}