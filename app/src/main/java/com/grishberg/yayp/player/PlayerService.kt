package com.grishberg.yayp.player

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
import com.grishberg.yayp.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


private const val TAG = "[DEBUG]"
//private const val ID = "DqHa4WUJatc"
private const val ID = "sBllMIaA8to"
private const val NOTIFICATION_ID = 1234

class PlayerService : Service(), Player {
    private val mediaPlayer = MediaPlayer()
    private val localBinder: IBinder = LocalBinder()
    private val extractor = YouTubeExtractor.Builder().build()
    override val mediaController = MediaPlayerControlImpl()

    override fun onBind(intent: Intent): IBinder {
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
        prepareMediaPlayer(id)
    }

    override fun stop() {
        mediaPlayer.stop()
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
            .setSmallIcon(R.mipmap.ic_launcher)
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
            //mediaPlayer.start()
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
        mediaPlayer.setDisplay(surfaceHolder)
    }

    override fun detachView() {
        Log.d(TAG, "detachView")
        mediaPlayer.setDisplay(null)
    }

    inner class MediaPlayerControlImpl : MediaController.MediaPlayerControl {
        override fun isPlaying(): Boolean = mediaPlayer.isPlaying

        override fun canSeekForward(): Boolean = mediaPlayer.isPlaying

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

    inner class LocalBinder : Binder() {
        fun getService(): Player {
            // Return this instance of LocalService so clients can call public methods
            return this@PlayerService
        }
    }

}