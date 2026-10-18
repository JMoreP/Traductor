package com.example.traductorlsv.ui.screens.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.traductorlsv.core.data.preferences.PreferencesManager
import com.example.traductorlsv.navigation.Route
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class SplashNavigationEvent {
    data class NavigateTo(val route: String) : SplashNavigationEvent()
}

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application.applicationContext)

    private val _navigationEvent = MutableSharedFlow<SplashNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        decideNextDestination()
    }

    private fun decideNextDestination() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val hasSeenOnboarding = preferencesManager.hasSeenOnboarding.first()

            // Asegura que la pantalla de splash dure al menos 1000 ms para evitar parpadeos visuales
            val elapsedTime = System.currentTimeMillis() - startTime
            val remainingDelay = (1000L - elapsedTime).coerceAtLeast(800L)
            delay(remainingDelay)

            val destination = if (hasSeenOnboarding) {
                Route.Home.route
            } else {
                Route.Onboarding.route
            }
            _navigationEvent.emit(SplashNavigationEvent.NavigateTo(destination))
        }
    }
}
