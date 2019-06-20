package com.grishberg.backgroundyoutubeplayer


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationCompat.PRIORITY_MIN
import android.util.Log
import android.view.SurfaceHolder
import android.widget.MediaController
import com.commit451.youtubeextractor.YouTubeExtraction
import com.commit451.youtubeextractor.YouTubeExtractor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


private const val TAG = "[DEBUG]"
private const val NOTIFICATION_ID = 1234

class PlayerService : Service(), Player {
    private val mediaPlayer = MediaPlayer()
    private val localBinder: IBinder = LocalBinder()
    private val extractor = YouTubeExtractor.Builder().build()

    private val prepared = Prepared()
    private val idle = Idle()
    private var state: State = idle

    private val mediaController = MediaPlayerControlImpl()

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind")
        return localBinder
    }

    override fun onCreate() {
        Log.d(TAG, "onCreated service")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return START_STICKY
    }

    override fun playVideo(id: String) {
        Log.d(TAG, "playVideo id=$id")
        prepareMediaPlayer(id)
    }

    override fun setMediaController(mediaController: MediaControllerFacade) {
        state.setMediaController(mediaController)
    }

    override fun stop() {
        mediaPlayer.stop()
        state.onStopped()
        stopForeground(true)
    }

    private fun startForeground() {

        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel()
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_notification_icon)
            .setPriority(PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = "my_service"
        val channelName = "My Background Service"
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_HIGH
        )
        chan.lightColor = Color.BLUE
        chan.importance = NotificationManager.IMPORTANCE_NONE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
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

        mediaPlayer.setOnInfoListener { mp, what, extra ->
            Log.d(TAG, "onInfo $what, $extra")
            false
        }
    }

    private fun bindVideoResult(extraction: YouTubeExtraction) {
        val streams = extraction.videoStreams
        if (streams.isEmpty()) {
            return
        }
        mediaPlayer.setDataSource(this, Uri.parse(streams[0].url))
        mediaPlayer.prepareAsync()
    }

    private fun onError(t: Throwable) {
        Log.e(TAG, "onError", t)
    }

    override fun attachView(surfaceHolder: SurfaceHolder) {
        Log.d(TAG, "attachView")
        state.attachView(surfaceHolder)
    }

    override fun detachView() {
        Log.d(TAG, "detachView")
        mediaPlayer.setDisplay(null)
    }

    inner class MediaPlayerControlImpl : MediaController.MediaPlayerControl {
        override fun isPlaying(): Boolean = mediaPlayer.isPlaying

        override fun canSeekForward(): Boolean = true

        override fun getDuration(): Int = mediaPlayer.duration

        override fun pause() {
            mediaPlayer.pause()
            stopForeground(false)
        }

        override fun getBufferPercentage(): Int = 0

        override fun seekTo(pos: Int) {
            mediaPlayer.seekTo(pos)
        }

        override fun getCurrentPosition(): Int = mediaPlayer.currentPosition

        override fun canSeekBackward(): Boolean = true

        override fun start() {
            mediaPlayer.start()
            startForeground()
        }

        override fun getAudioSessionId(): Int = mediaPlayer.audioSessionId

        override fun canPause(): Boolean = true
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
        private var mediaController: MediaControllerFacade? = null

        override fun onPrepared() {
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

        override fun setMediaController(mc: MediaControllerFacade) {
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

        override fun setMediaController(mc: MediaControllerFacade) {
            mc.setMediaPlayer(mediaController)
            mc.show()
        }
    }

    interface State {
        fun onPrepared() = Unit
        fun onStopped() = Unit
        fun attachView(surfaceHolder: SurfaceHolder) = Unit
        fun setMediaController(mc: MediaControllerFacade) = Unit
    }

}