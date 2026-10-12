package com.heruteguhapriyant.tasky.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heruteguhapriyant.tasky.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    data object Loading : SplashDestination()
    data object Onboarding : SplashDestination()
    data object Dashboard : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        checkOnboardingStatus()
    }

    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            // Short minimal delay for smooth splash impression
            delay(1000)
            val isCompleted = settingsRepository.isOnboardingCompleted().first()
            if (isCompleted) {
                _destination.value = SplashDestination.Dashboard
            } else {
                _destination.value = SplashDestination.Onboarding
            }
        }
    }
}
