package com.pemmob.luma.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _filter = MutableStateFlow(DashboardFilter.MONTH)
    val filter: StateFlow<DashboardFilter> = _filter.asStateFlow()

    init {
        loadData()
    }

    fun setFilter(filter: DashboardFilter) {
        _filter.value = filter
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                combine(repository.getDashboardData(), _filter) { data, currentFilter ->
                    // Filter sudah diterapkan di dalam repository, pass filter ke sana
                    data to currentFilter
                }.collect { (data, _) ->
                    _uiState.value = DashboardUiState.Success(data)
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Terjadi kesalahan yang tidak diketahui.")
            }
        }
    }

    fun refreshWithFilter(filter: DashboardFilter) {
        _filter.value = filter
        viewModelScope.launch {
            try {
                repository.getDashboardData(filter).collect { data ->
                    _uiState.value = DashboardUiState.Success(data)
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Gagal memuat data.")
            }
        }
    }
}
