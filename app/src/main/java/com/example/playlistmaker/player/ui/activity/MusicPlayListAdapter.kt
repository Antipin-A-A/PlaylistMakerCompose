package com.example.playlistmaker.player.ui.activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmakercompose.R
import com.example.playlistmaker.playlist.domain.model.PlayList
import com.example.playlistmaker.search.domain.api.OnItemClickListener

class MusicPlayListAdapter(private val onItemClickListener: OnItemClickListener<PlayList>) :
    RecyclerView.Adapter<MusicPlayListViewHolder>() {

    var playLists = mutableListOf<PlayList>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicPlayListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.music_playlist_adapter, parent, false)
        return MusicPlayListViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicPlayListViewHolder, position: Int) {
        holder.bind(playLists[position])
        holder.itemView.setOnClickListener { onItemClickListener.onItemClick(playLists[position]) }
    }

    override fun getItemCount(): Int = playLists.size

}