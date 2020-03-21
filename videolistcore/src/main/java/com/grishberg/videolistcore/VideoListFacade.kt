package com.grishberg.videolistcore

/**
 * Creates facade to manage video list.
 */
interface VideoListFacade {

    /**
     * Sets action for card click event.
     */
    fun setCardClickedAction(action: CardClickedAction)

    /**
     * Starts search videos.
     */
    fun searchVideos(searchString: String)
}