package com.pemmob.luma.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: StatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)

        viewModelScope.launch {
            _uiState.value = StatisticsUiState.Loading
            try {
                repository.getStatistics(year, month).collect { data ->
                    _uiState.value = StatisticsUiState.Success(data)
                }
            } catch (e: Exception) {
                _uiState.value = StatisticsUiState.Error(e.message ?: "Terjadi kesalahan yang tidak diketahui.")
            }
        }
    }
}
