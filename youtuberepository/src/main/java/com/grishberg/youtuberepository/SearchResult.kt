package com.grishberg.youtuberepository

import com.grishberg.youtuberepositorycore.VideoContainer

data class SearchResult(val nextId: String, val videos: List<VideoContainer>)