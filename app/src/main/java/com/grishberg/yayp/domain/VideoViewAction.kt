package com.grishberg.yayp.domain

import android.net.Uri

interface VideoViewAction {
    fun showAnimated() = Unit
    fun hideAnimated() = Unit
    fun play(stream: Uri) = Unit

    object STUB : VideoViewAction
}