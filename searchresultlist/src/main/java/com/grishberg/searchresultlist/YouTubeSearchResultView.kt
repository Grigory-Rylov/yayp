package com.grishberg.searchresultlist

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.grishberg.searchresultlist.rv.OnScrolledToEndAction
import com.grishberg.searchresultlist.rv.VideoAdapter
import com.grishberg.videolistcore.CardClickedAction
import com.grishberg.youtuberepositorycore.YouTubeRepository

class YouTubeSearchResultView @JvmOverloads constructor(
    private val youTubeRepository: YouTubeRepository,
    ctx: Context,
    attrs: AttributeSet? = null,
    style: Int = 0

) : RecyclerView(ctx, attrs, style), ClickableView {

    private val adapter = VideoAdapter(ctx)

    override var clickedAction: CardClickedAction
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    var loadMoreAction: OnScrolledToEndAction = OnScrolledToEndAction.STUB
    var nextId: String = ""

    private val layoutManager = LinearLayoutManager(ctx, RecyclerView.VERTICAL, false)

    init {
        addOnScrollListener(ScrollListener())
    }

    private inner class ScrollListener : OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()


            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                loadMoreAction.onScrolledToEnd()
            }

        }
    }
}