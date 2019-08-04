package com.grishberg.yayp.domain

interface PlayerLogic {
    fun onVideoClicked(id: String) = Unit
    fun onBackPressed() = Unit
    fun registerVideoViewAction(action: VideoView) = Unit
    fun unregisterVideoViewAction(action: VideoView) = Unit
    fun registerVideoListAction(action: VideoListAction) = Unit
    fun unregisterVideoListAction(action: VideoListAction) = Unit
    fun onSearchClicked(text: CharSequence) = Unit
    fun onSaveState(stateStorage: StateStorage)
    fun onViewCreated(state: RestoredState)
}

class PlayerLogicImpl : PlayerLogic {
    private var videoView: VideoView = VideoView.STUB
    private var videoListAction: VideoListAction = VideoListAction.STUB
    private val videoListState: State = VideoListState()
    private val videoState: State = VideoViewState()
    private var state: State = videoListState

    override fun onVideoClicked(id: String) {
        state.onVideoClicked(id)
    }

    override fun onBackPressed() {
        state.onBackPressed()
    }

    override fun onSaveState(stateStorage: StateStorage) {
        state.onSaveState(stateStorage)
    }

    override fun registerVideoViewAction(action: VideoView) {
        videoView = action
    }

    override fun unregisterVideoViewAction(action: VideoView) {
        videoView = VideoView.STUB
    }

    override fun registerVideoListAction(action: VideoListAction) {
        videoListAction = action
    }

    override fun unregisterVideoListAction(action: VideoListAction) {
        videoListAction = VideoListAction.STUB
    }

    override fun onSearchClicked(text: CharSequence) {
        state.search(text)
    }

    override fun onViewCreated(restoredState: RestoredState) {
        if (!restoredState.shouldRestoreState()) {
            return
        }

        if (restoredState.getMode() == ScreenMode.PLAYER) {
            restoreVideoState(restoredState)
        }
        restoreListMode(restoredState)
    }

    private fun restoreListMode(restoredState: RestoredState) {
        videoListAction.searchVideos(restoredState.searchString())
    }

    private fun restoreVideoState(restoredState: RestoredState) {
        state.onVideoClicked(restoredState.streamId())
        state.seekTo(restoredState.position())
    }

    private inner class VideoListState : State {
        private var searchString = ""
        override fun onBackPressed() {
            videoListAction.closeApp()
        }

        override fun onVideoClicked(id: String) {
            state = videoState
            videoListAction.hideAnimated()
            videoView.showAnimated()
            videoView.play(id)
            state.setVideoId(id)
        }

        override fun onSaveState(stateStorage: StateStorage) {
            stateStorage.setScreenMode(ScreenMode.LIST)
            stateStorage.setSearchString(searchString)
        }

        override fun search(string: CharSequence) {
            searchString = string.toString()
            videoListAction.searchVideos(string)
            videoListAction.hideKeyboard()
        }
    }

    private inner class VideoViewState : State {
        private var videoId: String = ""

        override fun onBackPressed() {
            state = videoListState
            videoView.stopPlay()
            videoView.hideAnimated()
            videoListAction.showAnimated()
        }

        override fun onSaveState(stateStorage: StateStorage) {
            stateStorage.setScreenMode(ScreenMode.PLAYER)
            stateStorage.setStreamId(videoId)
            stateStorage.setPosition(videoView.position())
        }

        override fun setVideoId(id: String) {
            videoId = id
        }

        override fun seekTo(position: Int) {
            videoView.seekTo(position)
        }
    }

    private interface State {
        fun onVideoClicked(id: String) = Unit
        fun onBackPressed() = Unit
        fun onSaveState(stateStorage: StateStorage) = Unit
        fun setVideoId(id: String) = Unit
        fun search(searchString: CharSequence) = Unit
        fun seekTo(position: Int) = Unit
    }

}