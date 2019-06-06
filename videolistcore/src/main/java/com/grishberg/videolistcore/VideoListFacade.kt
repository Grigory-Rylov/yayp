package com.grishberg.videolistcore

import android.view.View

interface VideoListFacade {
    fun createVideoListView(): View
    fun setCardClickedAction(action: CardClickedAction)
    fun searchVideos(searchString: String)
}