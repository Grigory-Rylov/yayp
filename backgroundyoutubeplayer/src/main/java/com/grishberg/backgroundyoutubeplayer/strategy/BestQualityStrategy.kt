package com.grishberg.backgroundyoutubeplayer.strategy

import com.commit451.youtubeextractor.Stream

class BestQualityStrategy : SelectStreamStrategy {
    private val comparator = BestQualityComparator()

    override fun selectStream(streams: List<Stream>, callback: (Stream) -> Unit) {
        val videoStreams = mutableListOf<Stream.VideoStream>()
        val audioStreams = mutableListOf<Stream.AudioStream>()
        for (stream in streams) {
            when (stream) {
                is Stream.VideoStream -> videoStreams.add(stream)
                is Stream.AudioStream -> audioStreams.add(stream)
            }
        }

        videoStreams.sortWith(comparator)
        callback.invoke(videoStreams.first())
    }

    class BestQualityComparator : Comparator<Stream.VideoStream> {
        override fun compare(o1: Stream.VideoStream, other: Stream.VideoStream): Int {
            if (other.resolution == o1.resolution) {
                return 0
            }
            val ourResolutionSplit = o1.resolution.split("p")
            val ourResolution = ourResolutionSplit.first().toIntOrNull() ?: 0
            val ourFrameRate = ourResolutionSplit[1].toIntOrNull() ?: 0
            val theirResolutionSplit = other.resolution.split("p")
            val theirResolution = theirResolutionSplit.first().toIntOrNull() ?: 0
            val theirFrameRate = theirResolutionSplit[1].toIntOrNull() ?: 0
            return if (ourResolution == theirResolution) {
                ourFrameRate.compareTo(theirFrameRate)
            } else {
                theirResolution.compareTo(ourResolution)
            }
        }
    }
}