package com.grishberg.searchresultlist.rv

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.grishberg.searchresultlist.R

internal class VideoItemHolder(v: View) : RecyclerView.ViewHolder(v) {
    val mTitleText: TextView
    val mDescriptionText: TextView
    val mThumbnailImage: ImageView
    val mShareIcon: ImageView
    val mShareText: TextView
    val mDurationText: TextView
    val mViewCountText: TextView
    val mLikeCountText: TextView
    val mDislikeCountText: TextView

    init {
        mTitleText = v.findViewById(R.id.video_title) as TextView
        mDescriptionText = v.findViewById(R.id.video_description) as TextView
        mThumbnailImage = v.findViewById(R.id.video_thumbnail) as ImageView
        mShareIcon = v.findViewById(R.id.video_share) as ImageView
        mShareText = v.findViewById(R.id.video_share_text) as TextView
        mDurationText = v.findViewById(R.id.video_dutation_text) as TextView
        mViewCountText = v.findViewById(R.id.video_view_count) as TextView
        mLikeCountText = v.findViewById(R.id.video_like_count) as TextView
        mDislikeCountText = v.findViewById(R.id.video_dislike_count) as TextView
    }
}