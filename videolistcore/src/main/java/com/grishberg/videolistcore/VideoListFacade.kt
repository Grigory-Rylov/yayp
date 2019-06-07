package com.grishberg.videolistcore

import android.view.View

/**
 * Creates facade to manage video list.
 */
interface VideoListFacade {
    /**
     * Creates video list view, that need to add manually to your application.
     */
    fun createVideoListView(): View

    /**
     * Sets action for card click event.
     */
    fun setCardClickedAction(action: CardClickedAction)

    /**
     * Starts search videos.
     */
    fun searchVideos(searchString: String)
}