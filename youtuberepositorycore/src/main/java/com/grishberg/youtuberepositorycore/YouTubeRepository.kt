package com.grishberg.youtuberepositorycore

interface YouTubeRepository {
    fun setPageDownloadedAction(action: OnPageDownloadedAction)
    fun search(searchString: String, nextToken: String)

    interface OnPageDownloadedAction {
        fun onPageDownloaded(nextPageId: String, videos: List<VideoContainer>)

        object STUB : OnPageDownloadedAction {
            override fun onPageDownloaded(nextPageId: String, videos: List<VideoContainer>) {
                /* stub */
            }
        }
    }
}