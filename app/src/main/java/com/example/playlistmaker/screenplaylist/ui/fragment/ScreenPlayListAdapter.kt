package com.example.playlistmaker.screenplaylist.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmakercompose.R
import com.example.playlistmaker.search.domain.api.OnItemClickListener
import com.example.playlistmaker.search.domain.api.OnItemLongClickListener
import com.example.playlistmaker.search.domain.modeles.Track
import com.example.playlistmaker.search.ui.activity.MusicViewHolder

class ScreenPlayListAdapter(private val onItemClickListener: OnItemClickListener<Track>, private val onItemLongClickListener: OnItemLongClickListener) :

    RecyclerView.Adapter<MusicViewHolder>() {

    var tracks = mutableListOf<Track>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_music_adapter, parent, false)
        return MusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        holder.bind(tracks[position])
        holder.itemView.setOnClickListener { onItemClickListener.onItemClick(tracks[position]) }
        holder.itemView.setOnLongClickListener { onItemLongClickListener.onItemLongClick(tracks[position])
            true }
    }

    override fun getItemCount(): Int {
        return tracks.size
    }
}