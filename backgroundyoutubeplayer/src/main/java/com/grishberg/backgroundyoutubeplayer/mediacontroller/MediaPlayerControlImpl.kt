package com.grishberg.backgroundyoutubeplayer.mediacontroller

import android.media.MediaPlayer
import android.widget.MediaController

class MediaPlayerControlImpl(
    private val mediaPlayer: MediaPlayer,
    private val playPauseAction: PlayPauseAction
) : MediaController.MediaPlayerControl {
    override fun isPlaying(): Boolean = mediaPlayer.isPlaying

    override fun canSeekForward(): Boolean = true

    override fun getDuration(): Int = mediaPlayer.duration

    override fun pause() {
        mediaPlayer.pause()
        playPauseAction.onPaused()
    }

    override fun getBufferPercentage(): Int = 0

    override fun seekTo(pos: Int) {
        mediaPlayer.seekTo(pos)
    }

    override fun getCurrentPosition(): Int = mediaPlayer.currentPosition

    override fun canSeekBackward(): Boolean = true

    override fun start() {
        mediaPlayer.start()
        playPauseAction.onStartPlaying()
    }

    override fun getAudioSessionId(): Int = mediaPlayer.audioSessionId

    override fun canPause(): Boolean = true
}