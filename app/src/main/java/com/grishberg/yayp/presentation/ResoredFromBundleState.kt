package com.grishberg.yayp.presentation

import android.os.Bundle
import com.grishberg.yayp.domain.RestoredState
import com.grishberg.yayp.domain.ScreenMode

private const val SCREEN_MODE = "ScreenMode"
private const val PLAYING_STATUS = "PlayingStatus"
private const val POSITION = "Position"
private const val STREAM_ID = "StreamId"
private const val SEARCH_STRING = "SearchString"

class ResoredFromBundleState(
    private val bundle: Bundle?
) : RestoredState {

    override fun shouldRestoreState(): Boolean = bundle != null


    override fun getMode(): ScreenMode {
        if(bundle == null) {
            return ScreenMode.LIST
        }
        return ScreenMode.values()[bundle.getInt(SCREEN_MODE, 0)]
    }

    override fun isPlaying(): Boolean {
        if(bundle == null) {
            return false
        }
        return bundle.getBoolean(PLAYING_STATUS)
    }

    override fun position(): Int {
        if(bundle == null) {
            return 0
        }
        return bundle.getInt(POSITION)
    }

    override fun streamId(): String {
        if (bundle == null) {
            return ""
        }
        return bundle.getString(STREAM_ID, "")
    }

    override fun searchString(): String {
        if (bundle == null) {
            return ""
        }
        return bundle.getString(SEARCH_STRING, "")
    }
}