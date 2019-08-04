package com.grishberg.yayp.presentation

import android.os.Bundle
import com.grishberg.yayp.domain.ScreenMode
import com.grishberg.yayp.domain.StateStorage

private const val SCREEN_MODE = "ScreenMode"
private const val PLAYING_STATUS = "PlayingStatus"
private const val POSITION = "Position"
private const val STREAM_ID = "StreamId"
private const val SEARCH_STRING = "SearchString"

class BundleStateStorage(
    private val bundle: Bundle
) : StateStorage {

    override fun setScreenMode(mode: ScreenMode) {
        bundle.putInt(SCREEN_MODE, mode.ordinal)
    }

    override fun setPlayingStatus(boolean: Boolean) {
        bundle.putBoolean(PLAYING_STATUS, boolean)
    }

    override fun setPosition(pos: Int) {
        bundle.putInt(POSITION, pos)
    }

    override fun setStreamId(id: String) {
        bundle.putString(STREAM_ID, id)
    }

    override fun setSearchString(searchString: String) {
        bundle.putString(SEARCH_STRING, searchString)
    }
}