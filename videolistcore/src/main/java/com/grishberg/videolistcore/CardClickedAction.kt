package com.grishberg.videolistcore

interface CardClickedAction {
    fun onCardClicked(cardId: String, title: String)
}