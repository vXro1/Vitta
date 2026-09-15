package com.vitta.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vitta.app.data.mock.PredefinedHabit

class HabitConfigViewModelFactory(private val habits: List<PredefinedHabit>) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HabitConfigViewModel(habits) as T
    }
}