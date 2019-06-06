package com.grishberg.searchresultlist

import android.content.Context
import android.view.View
import com.google.api.services.youtube.model.Video
import com.grishberg.searchresultlist.rv.OnScrolledToEndAction
import com.grishberg.searchresultlist.stubs.ClickableViewStub
import com.grishberg.videolistcore.CardClickedAction
import com.grishberg.videolistcore.VideoListFacade
import com.grishberg.youtuberepositorycore.VideoContainer
import com.grishberg.youtuberepositorycore.YouTubeRepository

/**
 * Implementation of video view facade.
 */
class VideoListFacadeImpl(
    private val contex: Context,
    private val youTubeRepository: YouTubeRepository
) : VideoListFacade {
    private var lastSearchString: String = ""
    private var nextId: String = ""

    private var clickableView: ClickableView = ClickableViewStub

    init {
        youTubeRepository.setPageDownloadedAction(OnNextPageLoadAction())
    }

    override fun createVideoListView(): View {
        val youTubeSearchResultView = YouTubeSearchResultView(youTubeRepository, contex)
        youTubeSearchResultView.loadMoreAction = ScrolledToEndAction()
        clickableView = youTubeSearchResultView
        return youTubeSearchResultView
    }

    override fun setCardClickedAction(action: CardClickedAction) {
        clickableView.clickedAction = action
    }

    override fun searchVideos(searchString: String) {
        youTubeRepository.search(searchString, "")
    }

    private inner class ScrolledToEndAction : OnScrolledToEndAction {
        override fun onScrolledToEnd() {
            youTubeRepository.search(lastSearchString, nextId)
        }
    }

    private inner class OnNextPageLoadAction : YouTubeRepository.OnPageDownloadedAction {

        override fun onPageDownloaded(nextPageId: String, videos: List<VideoContainer>) {

        }
    }
}