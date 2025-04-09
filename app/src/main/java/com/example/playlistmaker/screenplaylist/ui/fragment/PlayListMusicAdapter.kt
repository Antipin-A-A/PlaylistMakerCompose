package com.example.playlistmaker.screenplaylist.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.search.domain.api.OnItemClickListener
import com.example.playlistmaker.search.domain.modeles.Track
import com.example.playlistmakercompose.R

class PlayListMusicAdapter(private val onItemClickListener: OnItemClickListener<Track>) :

    RecyclerView.Adapter<PlayListMusicViewHolder>() {

    var tracks = mutableListOf<Track>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListMusicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_music_adapter, parent, false)
        return PlayListMusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayListMusicViewHolder, position: Int) {
        holder.bind(tracks[position])
        holder.itemView.setOnClickListener { onItemClickListener.onItemClick(tracks[position]) }
    }

    override fun getItemCount(): Int {
        return tracks.size
    }

}