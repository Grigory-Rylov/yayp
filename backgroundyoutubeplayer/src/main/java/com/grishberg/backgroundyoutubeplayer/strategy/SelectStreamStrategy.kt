package com.grishberg.backgroundyoutubeplayer.strategy

import com.commit451.youtubeextractor.Stream

interface SelectStreamStrategy {
    fun selectStream(streams: List<Stream>, callback: (Stream) -> Unit)
}