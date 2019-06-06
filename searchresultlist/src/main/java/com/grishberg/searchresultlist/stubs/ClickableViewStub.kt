package com.grishberg.searchresultlist.stubs

import com.grishberg.searchresultlist.ClickableView
import com.grishberg.videolistcore.CardClickedAction

/**
 * Stub of ClickableView
 */
internal object ClickableViewStub : ClickableView {
    override var clickedAction: CardClickedAction
        get() = CardClickedStubAction
        set(value) {}
}