package com.grishberg.searchresultlist.rv

interface OnScrolledToEndAction {
    fun onScrolledToEnd()

    object STUB : OnScrolledToEndAction {
        override fun onScrolledToEnd() { /* stub */ }
    }
}