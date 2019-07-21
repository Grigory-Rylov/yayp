package com.grishberg.searchresultlist.rv

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.grishberg.searchresultlist.R
import com.grishberg.videolistcore.CardClickedAction
import com.grishberg.youtuberepositorycore.VideoContainer
import com.squareup.picasso.Picasso
import java.text.DecimalFormat

internal class VideoAdapter(
    private val context: Context,
    private val clickedAction: CardClickedAction
) : RecyclerView.Adapter<VideoItemHolder>() {
    private val sFormatter = DecimalFormat("#,###,###")
    private val videos = ArrayList<VideoContainer>()

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoItemHolder {
        // inflate a card layout
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        // populate the viewholder

        val vh = VideoItemHolder(v)

        v.setOnClickListener {
            val pos = vh.adapterPosition
            clickedAction.onCardClicked(videos[pos].id, videos[pos].title, videos[pos].description)
        }
        return vh
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: VideoItemHolder, position: Int) {
        if (videos.isEmpty()) {
            return
        }

        val video = videos[position]
        holder.mTitleText.text = video.title
        holder.mDescriptionText.text = video.description
        // load the video thumbnail image
        Picasso.with(context)
            .load(video.thumbnailUrl)
            .placeholder(R.drawable.video_placeholder)
            .into(holder.mThumbnailImage)

        // create and set the click listener for both the share icon and share text
        val shareClickListener = View.OnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Watch \"" + video.title + "\" on YouTube")
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=" + video.id)
            sendIntent.type = "text/plain"
            context.startActivity(sendIntent)
        }
        holder.mShareIcon.setOnClickListener(shareClickListener)
        holder.mShareText.setOnClickListener(shareClickListener)

        // set the video duration text
        holder.mDurationText.text = parseDuration(video.duration)
        // set the video statistics
        holder.mViewCountText.text = sFormatter.format(video.viewCount)
        holder.mLikeCountText.text = sFormatter.format(video.likeCount)
        holder.mDislikeCountText.text = sFormatter.format(video.dislikeCount)

    }

    override fun getItemCount(): Int {
        return videos.size
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

    fun addVideo(videos: List<VideoContainer>) {
        this.videos.addAll(videos)
        notifyDataSetChanged()
    }

    fun clear() {
        videos.clear()
    }
}