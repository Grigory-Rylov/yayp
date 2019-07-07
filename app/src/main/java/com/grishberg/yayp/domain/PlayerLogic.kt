package com.grishberg.yayp.domain

class PlayerLogic {
    private var videoViewAction: VideoViewAction = VideoViewAction.STUB
    private var videoListAction: VideoListAction = VideoListAction.STUB
    private val videoList: State = VideoListState()
    private var state: State = videoList

    fun onVideoClicked(id: String) {
        videoListAction.hideAnimated()
        videoViewAction.showAnimated()
    }

    fun onBackPressed() {
        state.onBackPressed()
    }

    fun registerVideoViewAction(action: VideoViewAction) {
        videoViewAction = action
    }

    fun unregisterVideoViewAction(action: VideoViewAction) {
        videoViewAction = VideoViewAction.STUB
    }

    fun registerVideoListAction(action: VideoListAction) {
        videoListAction = action
    }

    fun unregisterVideoListAction(action: VideoListAction) {
        videoListAction = VideoListAction.STUB
    }

    private inner class VideoListState : State {
        override fun onBackPressed() {
            //TODO: exit from app
        }
    }

    private inner class VideoViewState : State {
        override fun onBackPressed() {
            videoViewAction.hideAnimated()
            videoListAction.showAnimated()
        }
    }

    private interface State {
        fun onBackPressed() = Unit
    }

}