package com.grishberg.yayp.domain

interface StateStorage {
    fun setScreenMode(mode: ScreenMode)
    fun setPlayingStatus(boolean: Boolean)
    fun setPosition(pos: Int)
    fun setStreamId(id: String)
    fun setSearchString(searchString: String)
}