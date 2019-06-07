package com.grishberg.videolistcore

interface CardClickedAction {
    fun onCardClicked(cardId: String, title: String, description: String)

    object STUB : CardClickedAction {
        override fun onCardClicked(cardId: String, title: String, description: String) = Unit
    }
}