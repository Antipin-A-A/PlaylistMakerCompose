package com.example.playlistmaker.search.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.base_room.domain.api.RoomInteract
import com.example.playlistmaker.search.Object.ERROR_CONNECT
import com.example.playlistmaker.search.domain.api.interactor.TrackIteractor
import com.example.playlistmaker.search.domain.modeles.Track
import com.example.playlistmaker.search.ui.state.TrackListState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SearchActivityViewModel(
    private val trackIteractor: TrackIteractor,
    private val roomInteract: RoomInteract
) : ViewModel() {

    private val stateMutable = MutableStateFlow<TrackListState>(TrackListState.Loading)
    val state = stateMutable.asStateFlow()

//    private val _observeMediaStateFlow = MutableStateFlow<TrackListState>(TrackListState.Loading)
//    val observeMediaStateFlow: StateFlow<TrackListState> = _observeMediaStateFlow.asStateFlow()

    init {
        getHistoryTrackList()
    }

    private var latestSearchText: String? = null
    private var searchJob: Job? = null
    fun saveHistoryTrack(track: Track) {
        trackIteractor.saveTrackList(track)
    }

    fun saveTrack(track: Track) {
        trackIteractor.saveTrack(track)
    }

    fun getHistoryTrackList() {
        viewModelScope.launch {
            val history = trackIteractor.loadTracksList()
            val favoriteTrack = check(history)
            renderState(TrackListState.GetHistoryList(track = favoriteTrack))
        }
    }

    private suspend fun check(trackHistory: List<Track>): List<Track> {
        val favoriteTracks = roomInteract.getTracksRoom().first()
        val updatedHistory = trackHistory.map { track ->
            track.copy(isFavorite = favoriteTracks.any { it.trackId == track.trackId })
        }
        return updatedHistory
    }

    fun removeHistoryTrackList() {
        trackIteractor.removeTrackList()
    }


/*    fun observeMediaState(): Flow<TrackListState> = flow {
        observeMediaStateFlow.collect { value ->
            emit(value)
        }
    }*/

    fun iTunesServiceSearch(searchText: String) {
        if (searchText.isNotEmpty()) {
            renderState(TrackListState.Loading)
            viewModelScope.launch {
                trackIteractor
                    .searchTrack(searchText)
                    .collect { pair ->
                        processResult(pair.first, pair.second)
                    }
            }
        } else {
            getHistoryTrackList()
        }
    }

    private fun processResult(foundTreks: List<Track>?, errorMessage: String?) {
        val trackSearch = mutableListOf<Track>()
        if (foundTreks != null) {
            trackSearch.addAll(foundTreks)
        }
        when {
            errorMessage != null -> {
                if (errorMessage == "$ERROR_CONNECT") {
                    renderState(TrackListState.Error(errorMessage))
                } else {
                    renderState(TrackListState.Empty)
                }
            }

            trackSearch.isEmpty() -> {
                renderState(TrackListState.Empty)
            }

            else -> {
                renderState(TrackListState.Content(trackSearch))
            }
        }
    }

    private fun renderState(state: TrackListState) {
        stateMutable.value = state
    }

    fun searchDebounce(changedText: String) {
        if (latestSearchText == changedText) {
            return
        }
        latestSearchText = changedText
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            iTunesServiceSearch(changedText)
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 1000L
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}