package com.grishberg.searchresultlist

import android.app.Activity
import com.grishberg.searchresultlist.rv.OnScrolledToEndAction
import com.grishberg.videolistcore.CardClickedAction
import com.grishberg.videolistcore.VideoListFacade
import com.grishberg.youtuberepositorycore.VideoContainer
import com.grishberg.youtuberepositorycore.YouTubeRepository

/**
 * Implementation of video view facade.
 */
class VideoListFacadeImpl(
    activity: Activity,
    private val youTubeRepository: YouTubeRepository,
    viewId: Int,
    isTablet: Boolean
) : VideoListFacade {
    private var lastSearchString: String = ""
    private var nextId: String = ""
    private val youTubeSearchResultView = activity.findViewById<YouTubeSearchResultView>(viewId)

    private var youTubeVideoListView: VideoListView = VideoListView.STUB

    init {
        youTubeRepository.setPageDownloadedAction(OnNextPageLoadAction())
        youTubeSearchResultView.initLayout(isTablet)
        youTubeSearchResultView.loadMoreAction = ScrolledToEndAction()
    }

    override fun setCardClickedAction(action: CardClickedAction) {
        youTubeVideoListView.clickedAction = action
    }

    override fun searchVideos(searchString: String) {
        youTubeVideoListView.clearList()
        youTubeRepository.search(searchString, "")
    }

    private inner class ScrolledToEndAction : OnScrolledToEndAction {
        override fun onScrolledToEnd() {
            youTubeRepository.search(lastSearchString, nextId)
        }
    }

    private inner class OnNextPageLoadAction : YouTubeRepository.OnPageDownloadedAction {

        override fun onPageDownloaded(nextPageId: String, videos: List<VideoContainer>) {
            nextId = nextPageId
            youTubeVideoListView.onSearchResultReceived(nextPageId, videos)
        }
    }
}