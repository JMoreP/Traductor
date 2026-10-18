package com.example.traductorlsv.ui.screens.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.traductorlsv.core.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class OnboardingEvent {
    object NavigateToHome : OnboardingEvent()
}

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application.applicationContext)

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events = _events.asSharedFlow()

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesManager.setHasSeenOnboarding(true)
            _events.emit(OnboardingEvent.NavigateToHome)
        }
    }
}
