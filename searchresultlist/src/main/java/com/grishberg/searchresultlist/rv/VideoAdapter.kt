package com.grishberg.searchresultlist.rv

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.api.services.youtube.model.Video
import com.grishberg.searchresultlist.R
import com.squareup.picasso.Picasso
import java.text.DecimalFormat

internal class VideoAdapter(
    private val context: Context
) : RecyclerView.Adapter<VideoItemHolder>() {
    private val sFormatter = DecimalFormat("#,###,###")
    var onScrollToEndAction: OnScrolledToEndAction = OnScrolledToEndAction.STUB
    private val mPlaylistVideos = ArrayList<Video>()

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoItemHolder {
        // inflate a card layout
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        // populate the viewholder
        return VideoItemHolder(v)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: VideoItemHolder, position: Int) {
        if (mPlaylistVideos.size === 0) {
            return
        }

        val video = mPlaylistVideos.get(position)
        val videoSnippet = video.getSnippet()
        val videoContentDetails = video.getContentDetails()
        val videoStatistics = video.getStatistics()

        holder.mTitleText.setText(videoSnippet.getTitle())
        holder.mDescriptionText.setText(videoSnippet.getDescription())

        // load the video thumbnail image
        Picasso.with(context)
            .load(videoSnippet.getThumbnails().getHigh().getUrl())
            .placeholder(R.drawable.video_placeholder)
            .into(holder.mThumbnailImage)

        // set the click listener to play the video
        holder.mThumbnailImage.setOnClickListener(View.OnClickListener {
            /*
            TODO: callba
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=" + video.getId())
                )
            )
            */
        })

        // create and set the click listener for both the share icon and share text
        val shareClickListener = View.OnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Watch \"" + videoSnippet.getTitle() + "\" on YouTube")
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=" + video.getId())
            sendIntent.type = "text/plain"
            context.startActivity(sendIntent)
        }
        holder.mShareIcon.setOnClickListener(shareClickListener)
        holder.mShareText.setOnClickListener(shareClickListener)

        // set the video duration text
        holder.mDurationText.setText(parseDuration(videoContentDetails.getDuration()))
        // set the video statistics
        holder.mViewCountText.setText(sFormatter.format(videoStatistics.getViewCount()))
        holder.mLikeCountText.setText(sFormatter.format(videoStatistics.getLikeCount()))
        holder.mDislikeCountText.setText(sFormatter.format(videoStatistics.getDislikeCount()))

        /*
        // get the next playlist page if we're at the end of the current page and we have another page to get
        val nextPageToken = mPlaylistVideos.getNextPageToken()
        if (!isEmpty(nextPageToken) && position == mPlaylistVideos.size() - 1) {
            holder.itemView.post(Runnable {
                onScrollToEndAction.onScrolledToEnd(nextPageToken)
            })
        }
        */

    }

    override fun getItemCount(): Int {
        return mPlaylistVideos.size
    }

    private fun isEmpty(s: String?): Boolean {
        return if (s == null || s.length == 0) {
            true
        } else false
    }

    private fun parseDuration(`in`: String): String {
        val hasSeconds = `in`.indexOf('S') > 0
        val hasMinutes = `in`.indexOf('M') > 0

        val s: String
        if (hasSeconds) {
            s = `in`.substring(2, `in`.length - 1)
        } else {
            s = `in`.substring(2, `in`.length)
        }

        var minutes = "0"
        var seconds = "00"

        if (hasMinutes && hasSeconds) {
            val split = s.split("M".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            minutes = split[0]
            seconds = split[1]
        } else if (hasMinutes) {
            minutes = s.substring(0, s.indexOf('M'))
        } else if (hasSeconds) {
            seconds = s
        }

        // pad seconds with a 0 if less than 2 digits
        if (seconds.length == 1) {
            seconds = "0$seconds"
        }

        return "$minutes:$seconds"
    }
}