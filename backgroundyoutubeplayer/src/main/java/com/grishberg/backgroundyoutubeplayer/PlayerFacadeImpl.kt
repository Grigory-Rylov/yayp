package com.grishberg.backgroundyoutubeplayer

import android.view.SurfaceView
import android.view.View
import com.example.backgroundplayer.PlayerFacade
import com.grishberg.backgroundyoutubeplayer.lifecycle.ActivityLifecycleAction
import com.grishberg.backgroundyoutubeplayer.lifecycle.ActivityLifecycleDelegate
import com.grishberg.common.Logger

private const val TAG = "PlayerFacade"

/**
 * This class lives as long as activity lives.
 */
class PlayerFacadeImpl(
    private val lifecycleDelegate: ActivityLifecycleDelegate,
    private val mediaController: MediaControllerFacade,
    private val videoView: SurfaceView,
    private val logger: Logger
) : PlayerFacade, ActivityLifecycleAction {

    private var playerService: Player = Player.STUB
    private val disconnected = Disconnected()
    private val connected = Connected()
    private var state: State = disconnected

    init {
        lifecycleDelegate.registerActivityLifeCycleAction(this)
    }

    override fun createView(): View {
        return videoView
    }

    override fun playStream(id: String) {
        state.playVideo(id)
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
            playerService.attachView(videoView.holder)
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