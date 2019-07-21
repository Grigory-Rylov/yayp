package com.grishberg.backgroundyoutubeplayer


import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.view.SurfaceHolder
import android.widget.MediaController
import com.commit451.youtubeextractor.YouTubeExtraction
import com.commit451.youtubeextractor.YouTubeExtractor
import com.grishberg.backgroundyoutubeplayer.mediacontroller.MediaPlayerControlImpl
import com.grishberg.backgroundyoutubeplayer.mediacontroller.PlayPauseAction
import com.grishberg.backgroundyoutubeplayer.notification.PlayerNotification
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

private const val NOTIFICATION_ID = 1234
private const val TAG = "[DEBUG]"

class PlayerService : Service(), Player, PlayPauseAction {
    private val mediaPlayer = MediaPlayer()
    private val localBinder: IBinder = LocalBinder()
    private val extractor = YouTubeExtractor.Builder().build()
    private val notification = PlayerNotification(this)

    private val prepared = Prepared()
    private val idle = Idle()
    private var state: State = idle
    private var screen: PlayerScreen = PlayerScreen.STUB

    private val mediaControllerState = MediaPlayerControlImpl(mediaPlayer, this)

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind")
        return localBinder
    }

    override fun onCreate() {
        Log.d(TAG, "onCreated service")
        val notification = notification.createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return START_STICKY
    }

    override fun playVideo(id: String) {
        Log.d(TAG, "playVideo id=$id")
        prepareMediaPlayer(id)
    }

    override fun setMediaController(mediaController: MediaController) {
        state.setMediaController(mediaController)
    }

    override fun stop() {
        mediaPlayer.stop()
        mediaPlayer.release()
        state.onStopped()
        stopForeground(true)
        mediaControllerState.onStoped()
    }

    override fun onStartPlaying() {
        val notification = notification.createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onPaused() {
        // TODO: update notification state.
    }


    override fun onDestroy() {
        Log.d(TAG, "service onDestroyed")
    }

    private fun prepareMediaPlayer(id: String) {
        extractor.extract(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ extraction ->
                bindVideoResult(extraction)
            }, { t ->
                onError(t)
            })

        mediaPlayer.setOnErrorListener(MediaPlayerErrorAction())

        mediaPlayer.setOnPreparedListener {
            Log.d(TAG, "prepared")
            state.onPrepared()
        }

        mediaPlayer.setOnInfoListener { _, what, extra ->
            Log.d(TAG, "onInfo $what, $extra")
            false
        }

        mediaPlayer.setOnVideoSizeChangedListener { _, width, height ->
            Log.d(TAG, "onSizeChanged w=$width h=$height")
            screen.updateScreenSize(width, height)
        }
    }

    private fun bindVideoResult(extraction: YouTubeExtraction) {
        val streams = extraction.videoStreams
        if (streams.isEmpty()) {
            return
        }
        try {
            mediaPlayer.stop()
            mediaPlayer.setDataSource(this, Uri.parse(streams[0].url))
            mediaPlayer.prepareAsync()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "setDataSource error", e)
        }
    }

    private fun onError(t: Throwable) {
        Log.e(TAG, "onError", t)
    }

    override fun attachView(surfaceHolder: SurfaceHolder, screen: PlayerScreen) {
        Log.d(TAG, "attachView")
        state.attachView(surfaceHolder)
        this.screen = screen
    }

    override fun detachView() {
        Log.d(TAG, "detachView")
        mediaPlayer.setDisplay(null)
        screen = PlayerScreen.STUB
    }

    inner class MediaPlayerErrorAction : MediaPlayer.OnErrorListener {
        override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
            Log.e(TAG, "error: what=$what, extra = $extra")
            return false
        }
    }

    inner class LocalBinder : Binder(), ServiceProvider {
        override fun getPlayer(): Player {
            return this@PlayerService
        }
    }

    inner class Idle : State {
        private var surfaceHolder: SurfaceHolder? = null
        private var mediaController: MediaController? = null

        override fun onPrepared() {
            mediaControllerState.onPrepared()
            state = prepared
            if (surfaceHolder != null) {
                state.attachView(surfaceHolder!!)
                surfaceHolder = null
            }
            if (mediaController != null) {
                state.setMediaController(mediaController!!)
                mediaController = null
            }
        }

        override fun attachView(sh: SurfaceHolder) {
            surfaceHolder = sh
        }

        override fun setMediaController(mc: MediaController) {
            mediaController = mc
        }
    }

    inner class Prepared : State {
        override fun onStopped() {
            state = idle
        }

        override fun attachView(surfaceHolder: SurfaceHolder) {
            mediaPlayer.setDisplay(surfaceHolder)
        }

        override fun setMediaController(mc: MediaController) {
            mc.setMediaPlayer(mediaControllerState)
            mc.show()
        }
    }

    interface State {
        fun onPrepared() = Unit
        fun onStopped() = Unit
        fun attachView(surfaceHolder: SurfaceHolder) = Unit
        fun setMediaController(mc: MediaController) = Unit
    }

}