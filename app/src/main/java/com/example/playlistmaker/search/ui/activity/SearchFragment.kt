package com.example.playlistmaker.search.ui.activity

import SearchScreen
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.playlistmaker.search.ui.viewmodel.SearchActivityViewModel
import com.example.playlistmakercompose.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class SearchFragment : Fragment() {

    private val viewModel by viewModel<SearchActivityViewModel>()
    private var isClickAllowed = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ):
            View {
        return ComposeView(requireContext()).apply {
            setContent {
                val navController = findNavController()
                val trackListState = viewModel.state.collectAsStateWithLifecycle()
                val bottomNavigationView =
                    requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation)
                SearchScreen(
                    trackListState.value,
                    viewModel = koinViewModel(),
                    navController,
                    bottomNavigationView,
                    clickDebounce()
                )
            }
        }
    }

    private fun clickDebounce(): Boolean {
        if (isClickAllowed) {
            isClickAllowed = false
            viewLifecycleOwner.lifecycleScope.launch {
                delay(500L)
                isClickAllowed = true
            }
        }
        return true
    }
}
