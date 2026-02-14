package com.wallshift.app.ui.onboarding

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallshift.app.domain.model.WallpaperTarget
import com.wallshift.app.domain.repository.SettingsRepository
import com.wallshift.app.domain.usecase.ApplyWallpaperUseCase
import com.wallshift.app.domain.usecase.FetchWallpaperUseCase
import com.wallshift.app.domain.usecase.ScheduleUseCase
import com.wallshift.app.domain.usecase.SmartSelectionUseCase
import com.wallshift.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val step: Int = 1,
    val selectedCategories: Set<String> = emptySet(),
    val customCategories: Set<String> = emptySet(),
    val frequencyMinutes: Int = Constants.DEFAULT_FREQUENCY_MINUTES,
    val wallpaperTarget: WallpaperTarget = WallpaperTarget.BOTH,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val fetchWallpaperUseCase: FetchWallpaperUseCase,
    private val smartSelectionUseCase: SmartSelectionUseCase,
    private val applyWallpaperUseCase: ApplyWallpaperUseCase,
    private val scheduleUseCase: ScheduleUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    val isOnboarded = settingsRepository.isOnboarded()

    fun toggleCategory(category: String) {
        val current = _uiState.value.selectedCategories
        val updated = if (category in current) current - category else current + category
        _uiState.value = _uiState.value.copy(selectedCategories = updated)
    }

    fun addCustomCategory(query: String) {
        val trimmed = query.trim().lowercase()
        if (trimmed.isBlank()) return
        val current = _uiState.value
        _uiState.value = current.copy(
            customCategories = current.customCategories + trimmed,
            selectedCategories = current.selectedCategories + trimmed,
        )
    }

    fun removeCustomCategory(query: String) {
        val current = _uiState.value
        _uiState.value = current.copy(
            customCategories = current.customCategories - query,
            selectedCategories = current.selectedCategories - query,
        )
    }

    fun setFrequency(minutes: Int) {
        _uiState.value = _uiState.value.copy(frequencyMinutes = minutes)
    }

    fun setWallpaperTarget(target: WallpaperTarget) {
        _uiState.value = _uiState.value.copy(wallpaperTarget = target)
    }

    fun nextStep() {
        _uiState.value = _uiState.value.copy(step = 2, error = null)
    }

    fun previousStep() {
        _uiState.value = _uiState.value.copy(step = 1, error = null)
    }

    fun completeOnboarding(onComplete: () -> Unit) {
        val state = _uiState.value
        if (state.selectedCategories.isEmpty() && state.customCategories.isEmpty()) {
            _uiState.value = state.copy(error = "Please select at least one category")
            return
        }

        _uiState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // Save settings
                settingsRepository.setSelectedCategories(state.selectedCategories)
                settingsRepository.setCustomCategories(state.customCategories)
                settingsRepository.setFrequencyMinutes(state.frequencyMinutes)
                settingsRepository.setWallpaperTarget(state.wallpaperTarget)
                settingsRepository.setAutoChangeEnabled(true)

                // Fetch and apply first wallpaper
                val candidates = fetchWallpaperUseCase(state.selectedCategories)
                if (candidates.isNotEmpty()) {
                    val deviceInfo = getDeviceInfo()
                    val selected = smartSelectionUseCase(candidates, deviceInfo)
                    if (selected != null) {
                        applyWallpaperUseCase(selected, state.wallpaperTarget)
                    }
                }

                // Schedule periodic changes
                scheduleUseCase.schedule(state.frequencyMinutes)

                // Mark onboarded
                settingsRepository.setOnboarded(true)

                _uiState.value = _uiState.value.copy(isLoading = false)
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Something went wrong. Please try again.",
                )
            }
        }
    }

    private fun getDeviceInfo(): SmartSelectionUseCase.DeviceInfo {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        return SmartSelectionUseCase.DeviceInfo(
            screenWidth = metrics.widthPixels,
            screenHeight = metrics.heightPixels,
        )
    }
}
