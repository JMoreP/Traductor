package com.example.traductorlsv.ui.screens.games

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.traductorlsv.core.data.repository.DatasetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GamesUiState(
    val signs: List<DatasetRepository.SignInfo> = emptyList(),
    val totalSamples: Int = 0
) {
    val signsCount: Int get() = signs.size
    val hasTrainedSigns: Boolean get() = signs.isNotEmpty()
}

class GamesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DatasetRepository.getInstance(application)

    private val _uiState = MutableStateFlow(
        GamesUiState(
            signs = repository.signs.value,
            totalSamples = repository.totalSamples.value
        )
    )
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.signs.collect { signsList ->
                _uiState.update { it.copy(signs = signsList) }
            }
        }
        viewModelScope.launch {
            repository.totalSamples.collect { total ->
                _uiState.update { it.copy(totalSamples = total) }
            }
        }
    }
}
