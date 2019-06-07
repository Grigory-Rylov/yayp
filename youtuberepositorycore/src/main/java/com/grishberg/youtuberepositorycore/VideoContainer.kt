package com.grishberg.youtuberepositorycore

import java.math.BigInteger

data class VideoContainer(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val publishedAt: Long,
    var duration: String = "",
    var viewCount: BigInteger = BigInteger.valueOf(0L),
    var likeCount: BigInteger = BigInteger.valueOf(0L),
    var dislikeCount: BigInteger = BigInteger.valueOf(0L)
)