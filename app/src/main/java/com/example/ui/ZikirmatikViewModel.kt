package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppTheme
import com.example.data.DhikrRepository
import com.example.data.DhikrSession
import com.example.data.StaticData
import com.example.data.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class ZikirmatikViewModel(
    private val repository: DhikrRepository,
    private val themeRepository: ThemeRepository
) : ViewModel() {

    val allSessions: StateFlow<List<DhikrSession>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentTheme: StateFlow<AppTheme> = themeRepository.currentTheme

    val currentZikir = MutableStateFlow(StaticData.zikirler[0])
    val currentCount = MutableStateFlow(0)
    
    val targetCount = MutableStateFlow<Int?>(null)
    val showCelebration = MutableStateFlow(false)
    
    private val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    val quoteOfTheDay = StaticData.quotes[dayOfYear % StaticData.quotes.size]

    fun increment() {
        currentCount.value += 1
        if (targetCount.value != null && currentCount.value == targetCount.value) {
            showCelebration.value = true
        }
    }

    fun dismissCelebration() {
        showCelebration.value = false
    }

    fun setTarget(target: String) {
        targetCount.value = target.toIntOrNull()
    }

    fun reset() {
        currentCount.value = 0
        showCelebration.value = false
    }

    fun setZikir(zikir: String) {
        currentZikir.value = zikir
        currentCount.value = 0
        showCelebration.value = false
    }

    fun saveSession() {
        if (currentCount.value > 0) {
            viewModelScope.launch {
                repository.insert(
                    DhikrSession(
                        type = currentZikir.value,
                        count = currentCount.value
                    )
                )
                currentCount.value = 0
            }
        }
    }

    fun setTheme(theme: AppTheme) {
        themeRepository.setTheme(theme)
    }
}

class ZikirmatikViewModelFactory(
    private val repository: DhikrRepository,
    private val themeRepository: ThemeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ZikirmatikViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ZikirmatikViewModel(repository, themeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
