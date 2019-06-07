package com.grishberg.searchresultlist

import com.grishberg.videolistcore.CardClickedAction
import com.grishberg.youtuberepositorycore.VideoContainer

internal interface VideoListView {
    var clickedAction: CardClickedAction

    fun onSearchResultReceived(
        nextPageId: String,
        videos: List<VideoContainer>
    )

    object STUB : VideoListView {
        override var clickedAction: CardClickedAction = CardClickedAction.STUB

        override fun onSearchResultReceived(nextPageId: String, videos: List<VideoContainer>) = Unit
    }
}