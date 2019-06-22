package com.grishberg.backgroundyoutubeplayer.mediacontroller

import android.widget.MediaController

class MediaPlayerControllerStub : MediaController.MediaPlayerControl {
    override fun isPlaying(): Boolean = false
    override fun canSeekForward(): Boolean = false
    override fun getDuration(): Int = 0
    override fun pause() = Unit
    override fun getBufferPercentage(): Int = 0
    override fun seekTo(pos: Int) = Unit
    override fun getCurrentPosition(): Int = 0
    override fun canSeekBackward(): Boolean = false
    override fun start() = Unit
    override fun getAudioSessionId(): Int = 0
    override fun canPause(): Boolean = false
}