package com.grishberg.yayp.presentation

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import com.grishberg.searchresultlist.VideoListFacadeImpl
import com.grishberg.videolistcore.VideoListFacade
import com.grishberg.yayp.R
import com.grishberg.yayp.player.Player
import com.grishberg.yayp.player.PlayerService
import com.grishberg.yayp.player.PlayerStub
import com.grishberg.youtuberepository.YouTubeRepositoryImpl


class MainActivity : AppCompatActivity() {
    private val STUB: Player = PlayerStub()
    private val disconnected = Disconnected()
    private val connected = Connected()
    private var state: State = disconnected
    private val connection = ServiceConnectionImpl()
    private var player = STUB
    private val hander = Handler()
    private lateinit var surfaceView: SurfaceView
    private lateinit var mediaController: MediaController
    private lateinit var videoListFacade: VideoListFacade

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            createView()
        }

        surfaceView = findViewById(R.id.surfaceView)
        mediaController = MediaController(this)
        mediaController.setAnchorView(surfaceView)

        val startServiceIntent = Intent(this@MainActivity, PlayerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startServiceIntent)
        } else {
            startService(startServiceIntent)
        }
    }

    private fun createView() {
        val container: ViewGroup = findViewById(R.id.container)
        val youTubeRepository = YouTubeRepositoryImpl()
        videoListFacade = VideoListFacadeImpl(this, youTubeRepository)
        val viewList = videoListFacade.createVideoListView()
        viewList.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        container.addView(viewList)
    }

    private fun stopPlaying() {
        state.stopPlaying()
    }

    override fun onStart() {
        super.onStart()
        state.onStart()
    }

    override fun onStop() {
        super.onStop()
        state.onStop()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mediaController.show()
        return false
    }

    private fun showMediaController() {
        hander.post {
            mediaController.show()
        }
    }

    /** Defines callbacks for service binding, passed to bindService()  */
    private inner class ServiceConnectionImpl : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            binder: IBinder
        ) {
            state.onConnected((binder as PlayerService.LocalBinder).getService())
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            state.onDisconnected()
        }
    }

    private inner class Connected : State {
        override fun onConnected(service: Player) {
            player = service
            service.attachView(surfaceView.holder)
            mediaController.setMediaPlayer(service.mediaController)
            showMediaController()
        }

        override fun onDisconnected() {
            state = disconnected
        }

        override fun stopPlaying() {
            player.stop()
        }

        override fun onStop() {
            player = STUB
            unbindService(connection)
            onDisconnected()
        }
    }

    private inner class Disconnected : State {
        override fun onConnected(service: Player) {
            state = connected
            state.onConnected(service)
        }

        override fun onStart() {
            val intent = Intent(this@MainActivity, PlayerService::class.java)
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private interface State {
        fun onConnected(service: Player) = Unit
        fun onDisconnected() = Unit
        fun onStop() = Unit
        fun onStart() = Unit
        fun stopPlaying() = Unit
    }
}
