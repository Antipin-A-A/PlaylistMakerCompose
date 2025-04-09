package com.example.playlistmaker.media.ui.activity

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmakercompose.R
import com.example.playlistmakercompose.databinding.PlayListAdapterBinding
import com.example.playlistmaker.playlist.domain.model.PlayList

class PlayListViewHolder(item: View) : RecyclerView.ViewHolder(item) {

    private val binding = PlayListAdapterBinding.bind(item)

    fun bind(item: PlayList) = with(binding) {

        listName.text = item.listName

        countTrack.text =
            itemView.context.getString(R.string.count_track, item.listTracksId?.filterNotNull()?.size.toString())

        Glide.with(itemView)
            .load(item.urlImage)
            .placeholder(R.drawable.empty_image_group)
            .transform(
                CenterCrop(),
                RoundedCorners(10),
            )
            .into(imageMusicGroup)
        Log.i("LogAdapter", "itemView = ${item.urlImage}")
    }
}