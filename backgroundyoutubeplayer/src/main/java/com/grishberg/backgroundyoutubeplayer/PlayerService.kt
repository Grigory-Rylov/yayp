package com.grishberg.backgroundyoutubeplayer


import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.SurfaceHolder
import android.widget.MediaController
import androidx.core.app.NotificationManagerCompat
import com.commit451.youtubeextractor.Stream
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
    private var mediaPlayer = MediaPlayer()
    private val localBinder: IBinder = LocalBinder()
    private val extractor = YouTubeExtractor.Builder().build()
    private lateinit var mediaSessionCompat: MediaSessionCompat
    private lateinit var notificationBuilder: PlayerNotification

    private val playing = Playing()
    private val idle = Idle()
    private var state: State = idle
    private var screen: PlayerScreen = PlayerScreen.STUB

    private var mediaControllerState = MediaPlayerControlImpl(mediaPlayer, this)


    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (mediaPlayer.isPlaying) {
                state.pause()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind")
        return localBinder
    }

    override fun onCreate() {
        Log.d(TAG, "onCreated service")
        initMediaSession()
        initNoisyReceiver()
        notificationBuilder = PlayerNotification(this, this, mediaSessionCompat)
        //val notification = notificationBuilder.createNotification()
        //startForeground(NOTIFICATION_ID, notification)
    }

    private fun initNoisyReceiver() {
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(noisyReceiver, filter)
    }

    private fun initMediaSession() {
        mediaSessionCompat = MediaSessionCompat(this, TAG)
    }

    override fun seekTo(pos: Int) {
        state.seekTo(pos)
    }

    override fun getPosition(): Int {
        return state.position()
    }

    private fun prepareMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaControllerState = MediaPlayerControlImpl(mediaPlayer, this)
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return START_STICKY
    }

    override fun playVideo(id: String) {
        Log.d(TAG, "playVideo id=$id")
        extractStream(id)
    }

    private fun extractStream(id: String) {
        extractor.extract(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ extraction ->
                state.bindVideoResult(extraction)
            }, { t ->
                onError(t)
            })
    }

    override fun setMediaController(mediaController: MediaController) {
        state.setMediaController(mediaController)
    }

    override fun stop() {
        state.stop()
    }

    override fun onStartPlaying() {
        val notification = notificationBuilder.createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onPaused() {
        // TODO: update notification state.
    }


    override fun onDestroy() {
        Log.d(TAG, "service onDestroyed")
        state.onDestroy()
        unregisterReceiver(noisyReceiver)
        mediaSessionCompat.release()
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
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
        state.detachView()
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
        private var pendingPosition = 0

        override fun onPrepared() {
            state = playing
            mediaControllerState.onPrepared()

            if (surfaceHolder != null) {
                state.attachView(surfaceHolder!!)
            }
            if (mediaController != null) {
                state.setMediaController(mediaController!!)
            }
            state.play()
            if (pendingPosition > 0) {
                state.seekTo(pendingPosition)
                pendingPosition = 0
            }
        }

        override fun bindVideoResult(extraction: YouTubeExtraction) {
            val streams = extraction.streams
            if (streams.isEmpty()) {
                Log.d(TAG, "empty video streams")
                return
            }

            prepareMediaPlayer()
            val stream = streams.first()
            when (stream) {
                is Stream.VideoStream -> processVideo(stream)
                is Stream.AudioStream -> processAudio(stream)

            }
        }

        private fun processVideo(stream: Stream.VideoStream) {
            mediaPlayer.setDataSource(this@PlayerService, Uri.parse(stream.url))
            mediaPlayer.prepareAsync()
        }

        private fun processAudio(stream: Stream.AudioStream) {
            mediaPlayer.setDataSource(this@PlayerService, Uri.parse(stream.url))
            mediaPlayer.prepareAsync()
        }

        override fun attachView(s: SurfaceHolder) {
            surfaceHolder = s
        }

        override fun setMediaController(mc: MediaController) {
            mediaController = mc
        }

        override fun seekTo(pos: Int) {
            pendingPosition = pos
        }
    }

    inner class Playing : State {
        private var surfaceHolder: SurfaceHolder? = null
        private var mediaController: MediaController? = null
        override fun detachView() {
            mediaPlayer.setDisplay(null)
        }

        override fun bindVideoResult(extraction: YouTubeExtraction) {
            stop()
            state = idle
            if (surfaceHolder != null) {
                state.attachView(surfaceHolder!!)
            }
            if (mediaController != null) {
                state.setMediaController(mediaController!!)
            }
            state.bindVideoResult(extraction)
        }

        override fun stop() {
            mediaPlayer.reset()
            mediaPlayer.release()
            stopForeground(true)
            mediaControllerState.onStoped()
            state = idle
        }

        override fun attachView(sh: SurfaceHolder) {
            mediaPlayer.setDisplay(sh)
            surfaceHolder = sh
        }

        override fun setMediaController(mc: MediaController) {
            mediaController = mc
            mc.setMediaPlayer(mediaControllerState)
            mc.show()
        }

        override fun play() {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            } else {
                mediaPlayer.start()
            }
        }

        override fun onDestroy() {
            mediaPlayer.stop()
            mediaPlayer.release()
            state = idle
        }

        override fun seekTo(pos: Int) {
            mediaPlayer.seekTo(pos)
        }

        override fun position(): Int = mediaPlayer.currentPosition

        override fun pause() {
            mediaPlayer.pause()
        }
    }

    interface State {
        fun onPrepared() = Unit
        fun attachView(surfaceHolder: SurfaceHolder) = Unit
        fun setMediaController(mc: MediaController) = Unit
        fun stop() = Unit
        fun onDestroy() = Unit
        fun bindVideoResult(extraction: YouTubeExtraction) = Unit
        fun detachView() = Unit
        fun play() = Unit
        fun seekTo(pos: Int) = Unit
        fun position(): Int = 0
        fun pause() = Unit
    }

}