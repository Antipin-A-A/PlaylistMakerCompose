package com.example.playlistmaker.media.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlistmakercompose.R
import com.example.playlistmakercompose.databinding.FragmentPlaylistsBinding
import com.example.playlistmaker.media.ui.viewmodel.PlaylistsViewModel
import com.example.playlistmaker.playlist.domain.model.PlayList
import com.example.playlistmaker.playlist.ui.viewmodel.PlayListState
import com.example.playlistmaker.search.domain.api.OnItemClickListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FragmentPlaylists : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    val viewModel by activityViewModel<PlaylistsViewModel>()

    private lateinit var adapter: PlayListAdapter

    private var isClickAllowed = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe(viewLifecycleOwner) {
            render(it)
        }

        viewModel.fillData()

        val onItemClickListener = OnItemClickListener<PlayList> { playlist ->
            if (clickDebounce()) {
                viewModel.saveTrack(playlist)
                findNavController().navigate(
                    R.id.action_mediaFragment_to_screenPlaylistFragment,
                )
            }
        }
        adapter = PlayListAdapter(onItemClickListener)
        binding.playList.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.playList.adapter = adapter

        binding.buttonNewPlayList.setOnClickListener {
            val bundle = Bundle().apply {
                putString("source", "mediaFragment")
            }
            findNavController().navigate(
                R.id.action_mediaFragment_to_fragmentNewPlayList, bundle
            )
        }
    }

    private fun render(state: PlayListState) {
        when (state) {
            is PlayListState.Content -> showContent(state.playlist)
            is PlayListState.Empty -> showEmpty(getString(R.string.playlists_null))
            is PlayListState.Loading -> showLoading()
        }
    }

    private fun showLoading() = with(binding) {
        playList.isVisible = false
        placeholderMessage.isVisible = false
    }

    private fun showEmpty(message: String) = with(binding) {
        playList.isVisible = false
        placeholderMessage.isVisible = true
        binding.placeholderMessage.text = message
    }

    private fun showContent(playList: List<PlayList>) = with(binding) {
        binding.playList.visibility = View.VISIBLE
        placeholderMessage.visibility = View.GONE
        adapter.playLists.clear()
        adapter.playLists.addAll(playList)
        adapter?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = FragmentPlaylists()
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
