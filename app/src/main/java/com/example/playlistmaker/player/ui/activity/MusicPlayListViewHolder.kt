package com.example.playlistmaker.player.ui.activity

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmakercompose.R
import com.example.playlistmakercompose.databinding.MusicPlaylistAdapterBinding
import com.example.playlistmaker.playlist.domain.model.PlayList

class MusicPlayListViewHolder(item: View) : RecyclerView.ViewHolder(item) {

    private val binding = MusicPlaylistAdapterBinding.bind(item)

    fun bind(item: PlayList) = with(binding) {
//        val countTracks = if (item.listName?.length!! > 20) {
//            item.listName.take(17) + "..."
//        } else {
//            item.listName.toString()
//        }

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
    }
}