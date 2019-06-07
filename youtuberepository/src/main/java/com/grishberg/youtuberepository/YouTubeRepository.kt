package com.grishberg.youtuberepository

import android.text.TextUtils
import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import com.grishberg.youtuberepositorycore.VideoContainer
import com.grishberg.youtuberepositorycore.YouTubeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

private const val TAG = "YouTubeRepositoryImpl"
//see: https://developers.google.com/youtube/v3/docs/videos/list
private const val YOUTUBE_VIDEOS_PART =
    "snippet,contentDetails,statistics" // video resource properties that the response will include.
private const val YOUTUBE_VIDEOS_FIELDS =
    "items(id,snippet(title,description,thumbnails/high),contentDetails/duration,statistics)" // selector specifying which fields to include in a partial response.
private const val YOUTUBE_PLAYLIST_MAX_RESULTS = 10L

/**
 * Interface for data access to youtube.
 */
class YouTubeRepositoryImpl(
    private val apiKey: String
) : YouTubeRepository {
    private var resultAction: YouTubeRepository.OnPageDownloadedAction = YouTubeRepository.OnPageDownloadedAction.STUB
    private val jsonFactory = GsonFactory()
    private val transport = AndroidHttp.newCompatibleTransport()
    private val youTube = YouTube.Builder(transport, jsonFactory, null).setApplicationName("app name")
        .build()

    override fun search(searchString: String, pageToken: String) {
        //TODO: manage destroy event to stop task.
        GlobalScope.async(Dispatchers.IO) {
            val result = searchVideos(searchString, pageToken)
            GlobalScope.launch(context = Dispatchers.Main) {
                Log.d(TAG, "Dispatchers.Main thread=" + Thread.currentThread())
                resultAction.onPageDownloaded(result.nextId, result.videos)
            }
        }
    }

    private fun searchVideos(searchKeyword: String, pageToken: String): SearchResult {
        Log.d(TAG, "searchVideos thread=" + Thread.currentThread())
        val items = ArrayList<VideoContainer>()
        var nextPageToken = ""
        try {
            val search = youTube.search().list("id,snippet")
            search.key = apiKey
            search.type = "video"
            search.maxResults = YOUTUBE_PLAYLIST_MAX_RESULTS
            search.fields =
                "nextPageToken,items(id/videoId,snippet/publishedAt,snippet/title,snippet/description,snippet/thumbnails/high/url)"
            search.q = searchKeyword
            if (pageToken.length > 0) {
                search.pageToken = pageToken
            }

            val response = search.execute()
            val results = response.items
            nextPageToken = if (response.nextPageToken != null) response.nextPageToken else ""


            for (result in results) {
                val item = VideoContainer(
                    result.id.videoId,
                    result.snippet.title,
                    result.snippet.description,
                    result.snippet.thumbnails.high.url,
                    result.snippet.publishedAt.value
                )

                items.add(item)
            }

        } catch (e: IOException) {
            Log.e(TAG, "Could not search: $e")
        }
        val searchResult = SearchResult(nextPageToken, items)
        searchAndPopulateWithVideoInfo(searchResult)
        return searchResult
    }

    private fun searchAndPopulateWithVideoInfo(videos: SearchResult) {
        val videoIds = ArrayList<String>()

        for (item in videos.videos) {
            videoIds.add(item.id)
        }
        try {
            val videoListResponse = youTube.videos()
                .list(YOUTUBE_VIDEOS_PART)
                .setFields(YOUTUBE_VIDEOS_FIELDS)
                .setKey(apiKey)
                .setId(TextUtils.join(",", videoIds)).execute()


            for (i in 0 until videoIds.size) {
                val videoContainer = videos.videos[i]
                with(videoContainer) {
                    duration = videoListResponse.items[i].contentDetails.duration
                    viewCount = videoListResponse.items[i].statistics.viewCount
                    likeCount = videoListResponse.items[i].statistics.likeCount
                    dislikeCount = videoListResponse.items[i].statistics.dislikeCount
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "searchAndPopulateWithVideoInfo", e)
        }
    }

    override fun setPageDownloadedAction(action: YouTubeRepository.OnPageDownloadedAction) {
        resultAction = action
    }
}