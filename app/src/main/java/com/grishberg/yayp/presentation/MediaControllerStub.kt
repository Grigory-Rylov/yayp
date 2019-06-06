package com.grishberg.yayp.presentation

import android.widget.MediaController

object MediaControllerStub : MediaController.MediaPlayerControl {
    override fun isPlaying(): Boolean = false

    override fun canSeekForward(): Boolean = true

    override fun getDuration(): Int = 0

    override fun pause() = Unit

    override fun getBufferPercentage(): Int = 0

    override fun seekTo(pos: Int) = Unit

    override fun getCurrentPosition(): Int = 0

    override fun canSeekBackward(): Boolean = true

    override fun start() = Unit

    override fun getAudioSessionId(): Int = 0

    override fun canPause(): Boolean = true
}