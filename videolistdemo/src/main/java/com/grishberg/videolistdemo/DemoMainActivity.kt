package com.grishberg.videolistdemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
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
        val container: ViewGroup = findViewById(R.id.container)
        youTubeRepository = YouTubeRepositoryImpl(BuildConfig.API_KEY)
        videoListFacade = VideoListFacadeImpl(this, youTubeRepository)
        val viewList = videoListFacade.createVideoListView()
        viewList.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        container.addView(viewList)

        videoListFacade.setCardClickedAction(VideoClickedListener())
        videoListFacade.searchVideos("kotlin")
    }

    private inner class VideoClickedListener : CardClickedAction {
        override fun onCardClicked(cardId: String, title: String, desc: String) {
            Toast.makeText(this@DemoMainActivity, "clicked on id=${cardId}", Toast.LENGTH_SHORT).show()
        }
    }
}
