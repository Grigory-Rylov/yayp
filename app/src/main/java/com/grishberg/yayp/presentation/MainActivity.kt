package com.grishberg.yayp.presentation

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.backgroundplayer.PlayerFacade
import com.grishberg.backgroundyoutubeplayer.PlayerFacadeImpl
import com.grishberg.backgroundyoutubeplayer.PlayerService
import com.grishberg.backgroundyoutubeplayer.ServiceProvider
import com.grishberg.backgroundyoutubeplayer.lifecycle.ActivityLifecycleAction
import com.grishberg.backgroundyoutubeplayer.lifecycle.ActivityLifecycleDelegate
import com.grishberg.searchresultlist.VideoListFacadeImpl
import com.grishberg.videolistcore.CardClickedAction
import com.grishberg.videolistcore.VideoListFacade
import com.grishberg.yayp.BuildConfig
import com.grishberg.yayp.R
import com.grishberg.yayp.common.LogcatLogger
import com.grishberg.yayp.domain.PlayerLogic
import com.grishberg.yayp.domain.PlayerLogicImpl
import com.grishberg.yayp.domain.VideoListAction
import com.grishberg.yayp.domain.VideoView
import com.grishberg.youtuberepository.YouTubeRepositoryImpl


class MainActivity : AppCompatActivity(), ActivityLifecycleDelegate {
    private val logger = LogcatLogger()
    private val connection = ServiceConnectionImpl()
    private lateinit var surfaceView: SurfaceView
    private lateinit var videoListFacade: VideoListFacade
    private val lifecycleActions: ArrayList<ActivityLifecycleAction> = ArrayList()
    private lateinit var playerFacade: PlayerFacade
    private lateinit var playerLogic: PlayerLogic
    private val videoListAction = OnVideoListAction()
    private val videoViewAction = OnVideoViewAction()
    private lateinit var videoListContainer: ViewGroup
    private lateinit var searchText: EditText
    private lateinit var searchButton: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val instance = lastCustomNonConfigurationInstance
        if (instance is PlayerLogic) {
            playerLogic = instance
        } else {
            playerLogic = PlayerLogicImpl()
            playerLogic.registerVideoListAction(videoListAction)
            playerLogic.registerVideoViewAction(videoViewAction)
        }

        val startServiceIntent = Intent(this@MainActivity, PlayerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startService(startServiceIntent)
        } else {
            startService(startServiceIntent)
        }

        val container = findViewById<ViewGroup>(R.id.container)
        createVideoView(container)
        createVideoListView()

        if (savedInstanceState != null) {
            playerLogic.onViewCreated(ResoredFromBundleState(savedInstanceState))
        } else {
            playerLogic.onSearchClicked(resources.getString(R.string.initialSearchString))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        playerLogic.onSaveState(BundleStateStorage(outState))
    }

    override fun onDestroy() {
        super.onDestroy()
        playerLogic.unregisterVideoListAction(videoListAction)
        playerLogic.unregisterVideoViewAction(videoViewAction)
    }

    override fun onBackPressed() {
        playerLogic.onBackPressed()
    }

    private fun createVideoListView() {
        searchButton = findViewById(R.id.searchButton)
        videoListContainer = findViewById(R.id.playerListContainer)
        searchText = findViewById(R.id.searchText)

        val youTubeRepository = YouTubeRepositoryImpl(BuildConfig.API_KEY)
        val isTablet = resources.getBoolean(R.bool.isTablet)
        videoListFacade = VideoListFacadeImpl(this, youTubeRepository, R.id.videoList, isTablet)

        videoListFacade.setCardClickedAction(VideoClickedListener())
        searchButton.setOnClickListener {
            playerLogic.onSearchClicked(searchText.text)
        }
    }

    private fun createVideoView(container: ViewGroup) {
        surfaceView = SurfaceView(this)
        surfaceView.visibility = View.GONE

        playerFacade = PlayerFacadeImpl(
            this,
            this,
            surfaceView,
            logger
        )

        val lp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        surfaceView.layoutParams = lp
        container.addView(surfaceView)
    }

    override fun onRetainCustomNonConfigurationInstance(): Any {
        return playerLogic
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
        playerFacade.showControls()
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


    private inner class VideoClickedListener : CardClickedAction {
        override fun onCardClicked(cardId: String, title: String, desc: String) {
            playerLogic.onVideoClicked(cardId)
        }
    }

    private inner class OnVideoViewAction : VideoView {
        override fun showAnimated() {
            surfaceView.visibility = View.VISIBLE

        }

        override fun hideAnimated() {
            surfaceView.visibility = View.GONE

        }

        override fun play(videoId: String) {
            playerFacade.playVideoById(videoId)
        }

        override fun stopPlay() {
            playerFacade.stopPlaying()
        }

        override fun seekTo(position: Int) {
            playerFacade.setPosition(position)
        }

        override fun position(): Int = playerFacade.getPosition()
    }

    private inner class OnVideoListAction : VideoListAction {
        override fun hideAnimated() {
            videoListContainer.visibility = View.GONE
        }

        override fun showAnimated() {
            videoListContainer.visibility = View.VISIBLE
        }

        override fun closeApp() {
            finish()
        }

        override fun searchVideos(searchText: CharSequence) {
            videoListFacade.searchVideos(searchText.toString())
        }

        override fun hideKeyboard() {
            if (currentFocus != null) {
                val inputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
            }
        }
    }
}
