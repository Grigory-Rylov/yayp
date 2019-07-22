package com.grishberg.backgroundyoutubeplayer.mediacontroller

import android.media.MediaPlayer
import android.widget.MediaController

class MediaPlayerControlImpl(
    private val mediaPlayer: MediaPlayer,
    private val playPauseAction: PlayPauseAction
) : MediaController.MediaPlayerControl {
    private val idle = Idle()
    private val prepared = Prepared()
    private var state: State = idle

    fun onPrepared() {
        state = prepared
    }

    fun onStoped() {
        state = idle
    }

    override fun isPlaying(): Boolean = state.isPlaying

    override fun canSeekForward(): Boolean = state.canSeekForward()

    override fun getDuration(): Int = state.duration

    override fun pause() {
        state.pause()
    }

    override fun getBufferPercentage(): Int = 0

    override fun seekTo(pos: Int) {
        state.seekTo(pos)
    }

    override fun getCurrentPosition(): Int = state.currentPosition

    override fun canSeekBackward(): Boolean = state.canSeekBackward()

    override fun start() {
        state.start()
    }

    override fun getAudioSessionId(): Int = state.audioSessionId

    override fun canPause(): Boolean = state.canPause()

    private inner class Idle : State {
        override fun isPlaying() = false

        override fun canSeekForward() = false

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

    private inner class Prepared : State {
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

    private interface State : MediaController.MediaPlayerControl {

    }
}