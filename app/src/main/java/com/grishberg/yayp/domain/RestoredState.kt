package com.grishberg.yayp.domain

import com.grishberg.yayp.domain.ScreenMode

interface RestoredState {
    fun shouldRestoreState() : Boolean
    fun getMode() : ScreenMode
    fun isPlaying() : Boolean
    fun position() : Int
    fun streamId() : String
    fun searchString() : String
}