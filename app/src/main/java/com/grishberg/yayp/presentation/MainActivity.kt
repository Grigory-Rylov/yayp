package com.grishberg.yayp.presentation

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import com.example.backgroundplayer.PlayerFacade
import com.grishberg.backgroundyoutubeplayer.MediaControllerFacadeImpl
import com.grishberg.backgroundyoutubeplayer.PlayerFacadeImpl
import com.grishberg.backgroundyoutubeplayer.PlayerService
import com.grishberg.backgroundyoutubeplayer.ServiceProvider
import com.grishberg.backgroundyoutubeplayer.lifecycle.ActivityLifecycleAction
import com.grishberg.backgroundyoutubeplayer.lifecycle.ActivityLifecycleDelegate
import com.grishberg.searchresultlist.VideoListFacadeImpl
import com.grishberg.videolistcore.VideoListFacade
import com.grishberg.yayp.BuildConfig
import com.grishberg.yayp.R
import com.grishberg.yayp.common.LogcatLogger
import com.grishberg.youtuberepository.YouTubeRepositoryImpl


class MainActivity : AppCompatActivity(), ActivityLifecycleDelegate {
    private val logger = LogcatLogger()
    private val connection = ServiceConnectionImpl()
    private lateinit var surfaceView: SurfaceView
    private lateinit var mediaController: MediaController
    private lateinit var videoListFacade: VideoListFacade
    private val lifecycleActions: ArrayList<ActivityLifecycleAction> = ArrayList()
    private lateinit var playerFacade: PlayerFacade

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            //createView()
        }

        surfaceView = SurfaceView(this)
        mediaController = MediaController(this)

        playerFacade = PlayerFacadeImpl(
            this,
            MediaControllerFacadeImpl(mediaController),
            surfaceView,
            logger
        )
        mediaController.setAnchorView(surfaceView)
        playerFacade.playStream("DqHa4WUJatc")

        val container = findViewById<ViewGroup>(R.id.container)
        surfaceView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        container.addView(surfaceView)

        val startServiceIntent = Intent(this@MainActivity, PlayerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startServiceIntent)
        } else {
            startService(startServiceIntent)
        }
    }

    private fun createView() {
        val container: ViewGroup = findViewById(R.id.container)
        val youTubeRepository = YouTubeRepositoryImpl(BuildConfig.API_KEY)
        videoListFacade = VideoListFacadeImpl(this, youTubeRepository)
        val viewList = videoListFacade.createVideoListView()
        viewList.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        container.addView(viewList)
    }

    override fun registerActivityLifeCycleAction(action: ActivityLifecycleAction) {
        lifecycleActions.add(action)
    }

    override fun unregisterActivityLifeCycleAction(action: ActivityLifecycleAction) {
        lifecycleActions.remove(action)
    }

    override fun unbindService() {
        unbindService(connection)
    }

    override fun bindService() {
        val intent = Intent(this@MainActivity, PlayerService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStart() {
        super.onStart()
        notifyOnStart()
    }

    private fun notifyOnStart() {
        for (i in 0 until lifecycleActions.size) {
            lifecycleActions[i].onStarted()
        }
    }

    override fun onStop() {
        super.onStop()
        notifyOnStop()
    }

    private fun notifyOnStop() {
        for (i in 0 until lifecycleActions.size) {
            lifecycleActions[i].onStop()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mediaController.show()
        return false
    }

    /** Defines callbacks for service binding, passed to bindService()  */
    private inner class ServiceConnectionImpl : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            binder: IBinder
        ) {
            for (i in 0 until lifecycleActions.size) {
                lifecycleActions[i].onServiceConnected(binder as ServiceProvider)
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            for (i in 0 until lifecycleActions.size) {
                lifecycleActions[i].onServiceDisconnected()
            }
        }
    }

}
