package com.grishberg.searchresultlist.stubs

import com.grishberg.videolistcore.CardClickedAction

internal object CardClickedStubAction : CardClickedAction {
    override fun onCardClicked(cardId: String, title: String) = Unit
}