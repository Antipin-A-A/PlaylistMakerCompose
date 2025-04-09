package com.example.playlistmaker.settings.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.api.interact.ThemeInteractor
import com.example.playlistmaker.sharing.domain.api.interact.SharingInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val providerSwitchStatus: ThemeInteractor,
    private val providerSharing: SharingInteractor
) : ViewModel() {

    private val stateMutable = MutableStateFlow<Boolean>(false)
    val state = stateMutable.asStateFlow()

    init {
        getSwitchStatus()
    }

   private fun getSwitchStatus() {
       stateMutable.value = providerSwitchStatus.getSwitchStatus()
    }

    fun switchTheme(isChecked: Boolean) {
        providerSwitchStatus.switchIsChecked(isChecked)
        stateMutable.value = isChecked
    }

    fun shareAppLink() {
        providerSharing.shareApp()
    }

    fun sendSupport() {
        providerSharing.openSupport()
    }

    fun openTerms() {
        providerSharing.openTerms()
    }

}