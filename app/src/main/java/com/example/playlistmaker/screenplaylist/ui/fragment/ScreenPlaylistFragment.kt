package com.example.playlistmaker.screenplaylist.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.playlistmakercompose.R
import com.example.playlistmakercompose.databinding.FragmentScreenPlaylistBinding
import com.example.playlistmaker.playlist.domain.model.PlayList
import com.example.playlistmaker.screenplaylist.ui.viewmodel.PlayListScreenState
import com.example.playlistmaker.screenplaylist.ui.viewmodel.ScreenPlaylistViewModel
import com.example.playlistmaker.screenplaylist.ui.viewmodel.TrackListStateScreen
import com.example.playlistmaker.search.domain.api.OnItemClickListener
import com.example.playlistmaker.search.domain.api.OnItemLongClickListener
import com.example.playlistmaker.search.domain.modeles.Track
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ScreenPlaylistFragment : Fragment() {

    private var _binding: FragmentScreenPlaylistBinding? = null
    private val binding get() = _binding!!

    private lateinit var bottomSheetBehaviorPlayList: BottomSheetBehavior<View>
    private lateinit var bottomSheetBehaviorMenu: BottomSheetBehavior<View>

    private lateinit var adapter: ScreenPlayListAdapter
    private var isClickAllowed = true
    private val viewModel by viewModel<ScreenPlaylistViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScreenPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        behaviorCoordinate()
        bottomSheetBehaviorMenu = BottomSheetBehavior.from(binding.bottomSheetBehaviorMenu)
        bottomSheetBehaviorMenu.state = BottomSheetBehavior.STATE_HIDDEN

        viewModel.observeTrackState().observe(viewLifecycleOwner) {
            render(it)
        }
        viewModel.observePlayListState().observe(viewLifecycleOwner) {
            renderPlayList(it)
        }

        viewModel.getPlaylist()

        binding.namePlayList.setOnClickListener {
            bottomSheetBehaviorPlayList.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        val onItemClickListener = OnItemClickListener<Track> { track ->
            if (clickDebounce()) {
                viewModel.saveTrack(track)
                findNavController().navigate(
                    R.id.action_screenPlaylistFragment_to_musicFragment
                )
            }
        }

        val onItemLongClickListener = OnItemLongClickListener { track ->
            if (clickDebounce()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.want_delete_track))
                    .setMessage("")
                    .setNegativeButton(getString(R.string.no)) { dialog, which ->
                    }
                    .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                        viewModel.deleteTrack(track.trackId.toString())
                        viewModel.getPlaylist()
                    }
                    .show()
            }
        }

        adapter = ScreenPlayListAdapter(onItemClickListener, onItemLongClickListener)
        binding.trackList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.trackList.adapter = adapter


        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonShare.setOnClickListener {
            if (binding.trackList.isVisible) {
                viewModel.sharePlayList()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.playlist_isEmpty_for_share), Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.buttonDots.setOnClickListener {
            bottomSheetBehaviorMenu.state = BottomSheetBehavior.STATE_COLLAPSED
            binding.overlay.isVisible = true
            binding.bottomSheetBehaviorMenu.isVisible = true
            sheetBehavior()
            actionBehaviorButton()
        }
    }

    private fun render(state: TrackListStateScreen) {
        when (state) {
            is TrackListStateScreen.Loading -> showLoading()
            is TrackListStateScreen.Content -> showContent(state.track)
            is TrackListStateScreen.Empty -> empty()
        }
    }

    private fun showLoading() = with(binding) {
        trackList.isVisible = false
    }

    private fun showContent(track: List<Track>) = with(binding) {
        emptyPlayliast.isVisible = false
        trackList.isVisible = true
        adapter.tracks.clear()
        adapter.tracks.addAll(track)
        adapter.notifyDataSetChanged()
    }

    private fun empty() = with(binding) {
        trackList.isVisible = false
        emptyPlayliast.isVisible = true
    }

    private fun renderPlayList(state: PlayListScreenState) {
        when (state) {
            is PlayListScreenState.Loading -> showLoading()
            is PlayListScreenState.Content -> content(state.playlist)
        }
    }

    private fun content(playList: PlayList) = with(binding) {
        namePlayList.text = playList.listName ?: getString(R.string.artist_name)
        namePlaylistBehavior.text = playList.listName ?: getString(R.string.artist_name)
        description.text = playList.description ?: ""
        val countTrack = playList.listTracksId?.filterNotNull()?.size.toString()
        viewModel.observeString().observe(viewLifecycleOwner) { time ->
            timeAndQuantity.text = getString(R.string.count_track_time, time, countTrack)
        }
        countTracksBehavior.text = playList.listTracksId?.size.toString()

        IMAGE = playList.urlImage.toString()
        PLAYLISTID = playList.id.toString()

        Glide.with(this@ScreenPlaylistFragment)
            .load(playList.urlImage)
            .placeholder(R.drawable.empty_image_group)
            .into(binding.imageView)


        Glide.with(this@ScreenPlaylistFragment)
            .load(playList.urlImage)
            .placeholder(R.drawable.empty_image_group)
            .into(binding.imageBehavior)
    }

    private fun actionBehaviorButton() = with(binding) {

        textShareBehavior.setOnClickListener {
            binding.bottomSheetBehaviorMenu.isVisible = false
            binding.overlay.isVisible = false
            viewModel.sharePlayList()
        }

        textInformationBehavior.setOnClickListener {
            binding.overlay.isVisible = false
            binding.bottomSheetBehaviorMenu.isVisible = false
            val bundle = Bundle().apply {
                putString("source", "screenPlaylist")
                putString("listName", "${namePlayList.text}")
                putString("description", "${description.text}")
                putString("Image", IMAGE)
                putString("playlistId", PLAYLISTID)
                Log.i("IMAGE", "IMAGE = $IMAGE")
            }
            findNavController().navigate(
                R.id.action_screenPlaylistFragment_to_fragmentNewPlayList, bundle

            )
        }

        textDeleteBehavior.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.want_delete_playlist))
                .setMessage("")
                .setNegativeButton(getString(R.string.no)) { dialog, which ->
                }
                .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                    viewModel.deleteList()
                    findNavController().navigateUp()
                }
                .show()
        }
    }

    private fun sheetBehavior() {
        val overlay = binding.overlay
        bottomSheetBehaviorMenu.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        overlay.visibility = View.GONE
                    }

                    else -> {
                        overlay.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun behaviorCoordinate() {
        val headerPosition = IntArray(2)
        bottomSheetBehaviorPlayList = BottomSheetBehavior.from(binding.bottomSheetBehaviorPlayList)
        bottomSheetBehaviorPlayList.state = BottomSheetBehavior.STATE_COLLAPSED
        binding.bottomSheetBehaviorPlayList.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {

                binding.buttonDots.getLocationOnScreen(headerPosition)
                val headerHeight = binding.buttonDots.height
                val peekHeight = headerPosition[1] - (headerHeight * 14)
                bottomSheetBehaviorPlayList.peekHeight = peekHeight

                binding.bottomSheetBehaviorPlayList.viewTreeObserver.removeOnGlobalLayoutListener(
                    this
                )
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (bottomSheetBehaviorMenu.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehaviorMenu.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        var IMAGE = "image"
        var PLAYLISTID = "playlistId"
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