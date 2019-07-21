package com.grishberg.yayp.domain

interface PlayerLogic {
    fun onVideoClicked(id: String) = Unit
    fun onBackPressed() = Unit
    fun registerVideoViewAction(action: VideoViewAction) = Unit
    fun unregisterVideoViewAction(action: VideoViewAction) = Unit
    fun registerVideoListAction(action: VideoListAction) = Unit
    fun unregisterVideoListAction(action: VideoListAction) = Unit
    fun onSearchClicked(text: CharSequence) = Unit
}

class PlayerLogicImpl : PlayerLogic {
    private var videoViewAction: VideoViewAction = VideoViewAction.STUB
    private var videoListAction: VideoListAction = VideoListAction.STUB
    private val videoList: State = VideoListState()
    private val videoView: State = VideoViewState()
    private var state: State = videoList

    override fun onVideoClicked(id: String) {
        state.onVideoClicked(id)
    }

    override fun onBackPressed() {
        state.onBackPressed()
    }

    override fun registerVideoViewAction(action: VideoViewAction) {
        videoViewAction = action
    }

    override fun unregisterVideoViewAction(action: VideoViewAction) {
        videoViewAction = VideoViewAction.STUB
    }

    override fun registerVideoListAction(action: VideoListAction) {
        videoListAction = action
    }

    override fun unregisterVideoListAction(action: VideoListAction) {
        videoListAction = VideoListAction.STUB
    }

    override fun onSearchClicked(text: CharSequence) {
        videoListAction.searchVideos(text)
        videoListAction.hideKeyboard()
    }

    private inner class VideoListState : State {
        override fun onBackPressed() {
            videoListAction.closeApp()
        }

        override fun onVideoClicked(id: String) {
            state = videoView
            videoListAction.hideAnimated()
            videoViewAction.showAnimated()
            videoViewAction.play(id)
        }
    }

    private inner class VideoViewState : State {
        override fun onBackPressed() {
            state = videoList
            videoViewAction.stopPlay()
            videoViewAction.hideAnimated()
            videoListAction.showAnimated()
        }
    }

    private interface State {
        fun onVideoClicked(id: String) = Unit
        fun onBackPressed() = Unit
    }

}