package com.grishberg.backgroundyoutubeplayer

import android.widget.MediaController

object MediaPlayerControlStub : MediaController.MediaPlayerControl {
    override fun isPlaying(): Boolean = false
    override fun canSeekForward(): Boolean = false
    override fun getDuration(): Int = -1
    override fun pause() = Unit
    override fun getBufferPercentage(): Int = 0
    override fun seekTo(p0: Int) = Unit
    override fun getCurrentPosition(): Int = -1
    override fun canSeekBackward(): Boolean = false
    override fun start() = Unit
    override fun getAudioSessionId(): Int = -1
    override fun canPause(): Boolean = false
}