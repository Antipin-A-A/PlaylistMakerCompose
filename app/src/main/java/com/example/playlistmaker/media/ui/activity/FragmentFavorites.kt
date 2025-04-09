package com.example.playlistmaker.media.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmakercompose.R
import com.example.playlistmakercompose.databinding.FragmentFavoritesBinding
import com.example.playlistmaker.media.ui.viewmodel.FavoriteState
import com.example.playlistmaker.media.ui.viewmodel.FavoritesViewModel
import com.example.playlistmaker.search.domain.api.OnItemClickListener
import com.example.playlistmaker.search.domain.modeles.Track
import com.example.playlistmaker.search.ui.activity.MusicAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FragmentFavorites : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModel<FavoritesViewModel>()

    private lateinit var adapter: MusicAdapter

    private var isClickAllowed = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onItemClickListener = OnItemClickListener<Track> { track ->
            if (clickDebounce()) {
                viewModel.saveTrack(track)
                findNavController().navigate(
                    R.id.action_mediaFragment_to_musicFragment
                )
            }
        }

        adapter = MusicAdapter(onItemClickListener)
        binding.trackList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.trackList.adapter = adapter

        viewModel.fillData()

        viewModel.observeState().observe(viewLifecycleOwner) {
            render(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun render(state: FavoriteState) {
        when (state) {
            is FavoriteState.Content -> showContent(state.movies)
            is FavoriteState.Empty -> showEmpty(getString(R.string.mediateka_null))
            is FavoriteState.Loading -> showLoading()
        }
    }

    private fun showLoading() = with(binding) {
        trackList.isVisible = false
        placeholderMessage.isVisible = false
    }

    private fun showEmpty(message: String) = with(binding) {
        trackList.isVisible = false
        placeholderMessage.isVisible = true
        binding.placeholderMessage.text = message
    }

    private fun showContent(tracks: List<Track>) = with(binding) {
        binding.trackList.visibility = View.VISIBLE
        placeholderMessage.visibility = View.GONE
        //    progressBar.visibility = View.GONE

        adapter.tracks.clear()
        adapter.tracks.addAll(tracks)
        adapter?.notifyDataSetChanged()
    }

    companion object {
        fun newInstance() = FragmentFavorites()
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    private fun clickDebounce(): Boolean {
        if (isClickAllowed) {
            isClickAllowed = false
            viewLifecycleOwner.lifecycleScope.launch {
                delay(CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }
        return true
    }
}