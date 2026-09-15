package com.vitta.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import com.vitta.app.data.mock.PredefinedHabit
import com.vitta.app.data.mock.predefinedHabitsCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HabitSelectionUiState(
    val allHabits: List<PredefinedHabit> = predefinedHabitsCatalog,
    val selectedFilter: String = "Todas",
    val selectedIds: Set<String> = emptySet()
) {
    val filteredHabits: List<PredefinedHabit>
        get() = if (selectedFilter == "Todas") allHabits
        else allHabits.filter { it.categoria == selectedFilter }

    val categories: List<String>
        get() = listOf("Todas") + allHabits.map { it.categoria }.distinct()
}

class HabitSelectionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HabitSelectionUiState())
    val uiState: StateFlow<HabitSelectionUiState> = _uiState.asStateFlow()

    fun onFilterSelect(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun toggleHabit(id: String) {
        val current = _uiState.value.selectedIds
        _uiState.value = _uiState.value.copy(
            selectedIds = if (id in current) current - id else current + id
        )
    }

    fun selectedHabits(): List<PredefinedHabit> =
        _uiState.value.allHabits.filter { it.id in _uiState.value.selectedIds }
}