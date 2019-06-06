package com.grishberg.youtuberepository

import com.google.api.services.youtube.model.Video

interface OnFoundAction {
    fun onFound(id: String, videos: List<Video>)
}