package com.grishberg.backgroundyoutubeplayer

import android.content.Context
import android.view.SurfaceView
import android.view.View
import android.widget.MediaController
import com.example.backgroundplayer.PlayerFacade
import com.grishberg.backgroundyoutubeplayer.lifecycle.ActivityLifecycleAction
import com.grishberg.backgroundyoutubeplayer.lifecycle.ActivityLifecycleDelegate
import com.grishberg.backgroundyoutubeplayer.mediacontroller.MediaPlayerControllerStub
import com.grishberg.common.Logger

private const val TAG = "PlayerFacade"

/**
 * This class lives as long as activity lives.
 */
class PlayerFacadeImpl(
    private val context: Context,
    private val lifecycleDelegate: ActivityLifecycleDelegate,
    private val videoView: SurfaceView,
    private val logger: Logger
) : PlayerFacade, ActivityLifecycleAction, PlayerScreen {

    private var playerService: Player = Player.STUB
    private val mediaController: MediaController

    private val disconnected = Disconnected()
    private val connected = Connected()
    private var state: State = disconnected

    init {
        lifecycleDelegate.registerActivityLifeCycleAction(this)
        mediaController = MediaController(context)
        mediaController.setMediaPlayer(MediaPlayerControllerStub())
        mediaController.setAnchorView(videoView)
    }

    override fun createView(): View {
        return videoView
    }

    override fun playVideoById(id: String) {
        state.playVideo(id)
    }

    override fun stopPlaying() {
        state.stopPlaying()
    }

    override fun onStarted() {
        state.onStart()
    }

    override fun onStop() {
        state.onStop()
    }

    override fun onServiceConnected(provider: ServiceProvider) {
        state.onConnected(provider.getPlayer())
    }

    override fun onServiceDisconnected() {
        state.onDisconnected()
    }

    override fun showControls() {
        mediaController.show()
    }

    override fun updateScreenSize(w: Int, h: Int) {
        val ratio = w.toFloat() / h.toFloat()
        val lp = videoView.layoutParams
        val newHeight = videoView.width / ratio
        lp.height = newHeight.toInt()
        videoView.requestLayout()
    }

    private inner class Connected : State {
        override fun onDisconnected() {
            state = disconnected
        }

        override fun stopPlaying() {
            playerService.stop()
        }

        override fun onStop() {
            playerService = Player.STUB
            lifecycleDelegate.unbindService()
            onDisconnected()
        }

        override fun playVideo(id: String) {
            playerService.playVideo(id)
        }
    }

    private inner class Disconnected : State {
        private var videoId = ""

        override fun onConnected(service: Player) {
            logger.d(TAG, "onConnected")
            playerService = service
            playerService.attachView(videoView.holder, this@PlayerFacadeImpl)
            playerService.setMediaController(mediaController)
            state = connected
            if (videoId.isNotEmpty()) {
                state.playVideo(videoId)
                videoId = ""
            }
        }

        override fun onStart() {
            lifecycleDelegate.bindService()
        }

        override fun playVideo(id: String) {
            videoId = id
        }
    }

    private interface State {
        fun onConnected(player: Player) = Unit
        fun onDisconnected() = Unit
        fun onStop() = Unit
        fun onStart() = Unit
        fun stopPlaying() = Unit
        fun playVideo(id: String) = Unit
    }

}