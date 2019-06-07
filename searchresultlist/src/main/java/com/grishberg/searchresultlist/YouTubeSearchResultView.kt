package com.grishberg.searchresultlist

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.grishberg.searchresultlist.rv.OnScrolledToEndAction
import com.grishberg.searchresultlist.rv.VideoAdapter
import com.grishberg.videolistcore.CardClickedAction
import com.grishberg.youtuberepositorycore.VideoContainer

class YouTubeSearchResultView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    style: Int = 0

) : RecyclerView(ctx, attrs, style), VideoListView {

    private val adapter = VideoAdapter(ctx, VideoClickedAction())

    override var clickedAction: CardClickedAction = CardClickedAction.STUB

    var loadMoreAction: OnScrolledToEndAction = OnScrolledToEndAction.STUB

    private val layoutManager = LinearLayoutManager(ctx, VERTICAL, false)

    init {
        addOnScrollListener(ScrollListener())
        setLayoutManager(layoutManager)
        setAdapter(adapter)
    }

    override fun onSearchResultReceived(nextPageId: String, videos: List<VideoContainer>) {
        adapter.addVideo(videos)
    }

    private inner class ScrollListener : OnScrollListener() {
        private var lastCheckedPosition: Int = -1
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()


            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                firstVisibleItemPosition >= 0 && totalItemCount > lastCheckedPosition
            ) {
                lastCheckedPosition = totalItemCount
                loadMoreAction.onScrolledToEnd()
            }
        }
    }

    private inner class VideoClickedAction : CardClickedAction {
        override fun onCardClicked(cardId: String, title: String, description: String) {
            clickedAction.onCardClicked(cardId, title, description)
        }
    }
}