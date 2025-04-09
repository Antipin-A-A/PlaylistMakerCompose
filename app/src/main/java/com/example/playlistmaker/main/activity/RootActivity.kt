package com.example.playlistmaker.main.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.playlistmakercompose.R
import com.example.playlistmakercompose.databinding.ActivityRootBinding

class RootActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.containerView) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.musicFragment -> {
                    binding.bottomNavigation.isVisible = false
                }

                R.id.fragmentNewPlayList -> {
                    binding.bottomNavigation.isVisible = false
                }
                R.id.screenPlaylistFragment ->{
                    binding.bottomNavigation.isVisible = false
                }

                else -> {
                    binding.bottomNavigation.isVisible = true
                }
            }
        }
    }
}