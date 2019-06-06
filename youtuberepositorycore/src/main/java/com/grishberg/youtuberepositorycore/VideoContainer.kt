package com.grishberg.youtuberepositorycore

data class VideoContainer(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl : String,
    val publishedAt : Long
)