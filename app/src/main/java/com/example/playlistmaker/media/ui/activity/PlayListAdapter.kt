package com.example.playlistmaker.media.ui.activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmakercompose.R
import com.example.playlistmaker.playlist.domain.model.PlayList
import com.example.playlistmaker.search.domain.api.OnItemClickListener

class PlayListAdapter(private val onItemClickListener: OnItemClickListener<PlayList>) :
    RecyclerView.Adapter<PlayListViewHolder>() {

    var playLists = mutableListOf<PlayList>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.play_list_adapter, parent, false)
        return PlayListViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayListViewHolder, position: Int) {
        holder.bind(playLists[position])
        holder.itemView.setOnClickListener { onItemClickListener.onItemClick(playLists[position]) }
    }

    override fun getItemCount(): Int = playLists.size


}