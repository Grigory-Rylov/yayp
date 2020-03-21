package com.grishberg.videolistdemo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.grishberg.searchresultlist.VideoListFacadeImpl
import com.grishberg.videolistcore.CardClickedAction
import com.grishberg.youtuberepository.YouTubeRepositoryImpl
import com.grishberg.youtuberepositorycore.YouTubeRepository

class DemoMainActivity : AppCompatActivity() {

    private lateinit var videoListFacade: VideoListFacadeImpl
    private lateinit var youTubeRepository: YouTubeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_main)
        createView()
    }

    private fun createView() {
        youTubeRepository = YouTubeRepositoryImpl(BuildConfig.API_KEY)
        val isTablet = resources.getBoolean(R.bool.isTablet)
        videoListFacade = VideoListFacadeImpl(this, youTubeRepository, R.id.videoList, isTablet)
        videoListFacade.setCardClickedAction(VideoClickedListener())
        videoListFacade.searchVideos("kotlin")
    }

    private inner class VideoClickedListener : CardClickedAction {
        override fun onCardClicked(cardId: String, title: String, desc: String) {
            Toast.makeText(this@DemoMainActivity, "clicked on id=${cardId}", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
