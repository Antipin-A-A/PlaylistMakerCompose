package com.example.playlistmaker.player.ui.activity


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmakercompose.R
import com.example.playlistmaker.app.ConnectBroadcastReceiver
import com.example.playlistmakercompose.databinding.FragmentMusicBinding
import com.example.playlistmaker.player.service.MusicService
import com.example.playlistmaker.player.ui.viewmodel.MusicFragmentViewModel
import com.example.playlistmaker.player.ui.viewmodel.PlayListStateForMusic
import com.example.playlistmaker.playlist.domain.model.PlayList
import com.example.playlistmaker.search.domain.api.OnItemClickListener
import com.example.playlistmaker.search.domain.modeles.Track
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class MusicFragment : Fragment() {

    private val connectBroadcastReceiver = ConnectBroadcastReceiver()
    private var _binding: FragmentMusicBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MusicPlayListAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private var isClickAllowed = true
    private var urlSong = ""
    private var artistNameSong = ""
    private var trackNameSong = ""


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            bindMusicService()
        } else {
            Toast.makeText(requireContext(), "Can't bind service!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMusicBinding.inflate(layoutInflater)
        return binding.root
    }

    private val viewModel by viewModel<MusicFragmentViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        viewModel.currentPosition.observe(viewLifecycleOwner) {
            updateProgress(it.time.toString())
        }

        viewModel.observeState().observe(viewLifecycleOwner) {
            render(it)
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                snake(message)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            bindMusicService()
        }

        viewModel.isFavorite.observe(viewLifecycleOwner) {
            if (it) {
                binding.buttonLike.setImageResource(R.drawable.haed_red)
            } else {
                binding.buttonLike.setImageResource(R.drawable.head_with)
            }
        }

        lifecycleScope.launch {
            viewModel.isPlaying.collect { isPlaying ->
                binding.buttonPlay.setPlaying(isPlaying)
            }
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message.startsWith(getString(R.string.Track_add))) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    private fun init() {

        binding.apply {
            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            content(viewModel.getTrack())

            val overlay = binding.overlay
            bottomSheetBehavior = BottomSheetBehavior.from(binding.standardBottomSheet2)

            buttonAdd.setOnClickListener {
                viewModel.fillData()
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            buttonPlay.setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    view.performClick()
                    viewModel.togglePlayback()

                }
                true
            }

            buttonLike.setOnClickListener {
                viewModel.onFavoriteClicked()
            }

            buttonNewPlayList.setOnClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                val bundle = Bundle().apply {
                    putString("source", "mediaFragment")
                }
                findNavController().navigate(
                    R.id.action_musicFragment_to_fragmentNewPlayList, bundle
                )
            }

            bottomSheetBehavior.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            overlay.visibility = View.GONE
                        }

                        else -> {
                            overlay.visibility = View.VISIBLE
                            adapter.notifyDataSetChanged()
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })

            val onItemClickListener = OnItemClickListener<PlayList> { playlist ->
                if (clickDebounce()) {
                    viewModel.saveFillData(playlist.id)
                }
            }

            adapter = MusicPlayListAdapter(onItemClickListener)
            binding.playList.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.playList.adapter = adapter
        }
    }

    private fun content(track: Track) = with(binding) {
        artistName.text = track.artistName ?: getString(R.string.artist_name)
        collectionNameFirst.text = track.trackName ?: getString(R.string.collection_name)
        collectionName.text = track.collectionName ?: getString(R.string.albom)
        trackTime.text = track.trackTimeMillis ?: getString(R.string.duration)
        releaseDate.text = track.releaseDate ?: getString(R.string.god)
        primaryGenreName.text = track.primaryGenreName ?: getString(R.string.primary_genre_name)
        country.text = track.country ?: getString(R.string.country)
        urlSong = track.previewUrl.toString()
        artistNameSong = artistName.text.toString()
        trackNameSong = collectionNameFirst.text.toString()
        Glide.with(this@MusicFragment)
            .load(track.artworkUrl100?.replaceAfterLast('/', "512x512bb.jpg"))
            .placeholder(R.drawable.empty_image_group)
            .fitCenter()
            .transform(RoundedCorners(10))
            .into(imageView)
    }

    override fun onPause() {
        super.onPause()
        viewModel.showNotification()
        requireContext().unregisterReceiver(connectBroadcastReceiver)
    }

    override fun onResume() {
        super.onResume()
        viewModel.hideNotification()
        requireContext().registerReceiver(
            connectBroadcastReceiver,
            IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        )
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    override fun onDestroyView() {
        viewModel.pause()
        viewModel.removeAudioPlayerControl()
        binding.buttonPlay.setPlaying(false)
        unbindMusicService()
        super.onDestroyView()
    }

    private fun snake(string: String) {
        val snack: Snackbar = Snackbar.make(requireView(), string, Snackbar.LENGTH_LONG)
        snack.setTextColor(resources.getColor(R.color.background))
        snack.show()
    }


    private fun render(state: PlayListStateForMusic) {
        when (state) {
            is PlayListStateForMusic.Content -> showContent(state.playlist)
            is PlayListStateForMusic.Loading -> showLoading()
        }
    }

    private fun showLoading() = with(binding) {
        playList.isVisible = false
    }

    private fun showContent(playList: List<PlayList>) = with(binding) {
        binding.playList.visibility = View.VISIBLE
        adapter.playLists.clear()
        adapter.playLists.addAll(playList)
        adapter.notifyDataSetChanged()
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

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicServiceBinder
            viewModel.setAudioPlayerControl(binder.getService())

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            viewModel.removeAudioPlayerControl()
        }
    }

    private fun bindMusicService() {
        val intent = Intent(requireContext(), MusicService::class.java).apply {
            putExtra("song_url", urlSong)
            putExtra("artist_name_song", artistNameSong)
            putExtra("track_name_song", trackNameSong)
            Log.i("LogUri-4", "track_name_song -$trackNameSong, artist_name_song- $artistNameSong")
        }
        requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindMusicService() {
        requireActivity().unbindService(serviceConnection)
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }


    private fun updateProgress(position: String) {
        binding.currentTrackTime.text = position
    }

}