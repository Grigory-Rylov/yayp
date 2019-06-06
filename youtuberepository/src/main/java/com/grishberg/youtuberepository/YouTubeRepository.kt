package com.grishberg.youtuberepository

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
        GlobalScope.async(Dispatchers.IO) {
            val result = doSearch(searchString, pageToken)
            GlobalScope.launch(context = Dispatchers.Main) {
                Log.d(TAG, "Dispatchers.Main thread=" + Thread.currentThread())
                resultAction.onPageDownloaded(result.nextId, result.videos)
            }
        }
    }

    private suspend fun doSearch(searchKeyword: String, pageToken: String): SearchResult {
        Log.d(TAG, "doSearch thread=" + Thread.currentThread())
        val items = ArrayList<VideoContainer>()
        var nextPageToken = ""
        try {
            val search = youTube.search().list("id,snippet")
            search.key = apiKey
            search.type = "video"
            search.fields =
                "items(id/videoId,snippet/publishedAt,snippet/title,snippet/description,snippet/thumbnails/default/url)"
            search.q = searchKeyword
            if (pageToken.length > 0) {
                search.pageToken = pageToken
            }

            val response = search.execute()
            val results = response.items
            nextPageToken = response.nextPageToken


            for (result in results) {
                val item = VideoContainer(
                    result.id.videoId,
                    result.snippet.title,
                    result.snippet.description,
                    result.snippet.thumbnails.default.url,
                    result.snippet.publishedAt.value
                )

                items.add(item)
            }

        } catch (e: IOException) {
            Log.e(TAG, "Could not search: $e")
        }
        return SearchResult(nextPageToken, items)
    }

    override fun setPageDownloadedAction(action: YouTubeRepository.OnPageDownloadedAction) {
        resultAction = action
    }
}