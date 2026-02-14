package com.wallshift.app.ui.settings

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallshift.app.data.cache.ImageCacheManager
import com.wallshift.app.domain.model.WallpaperTarget
import com.wallshift.app.domain.repository.SettingsRepository
import com.wallshift.app.domain.repository.WallpaperRepository
import com.wallshift.app.domain.usecase.ApplyWallpaperUseCase
import com.wallshift.app.domain.usecase.FetchWallpaperUseCase
import com.wallshift.app.domain.usecase.ScheduleUseCase
import com.wallshift.app.domain.usecase.SmartSelectionUseCase
import com.wallshift.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val selectedCategories: Set<String> = emptySet(),
    val customCategories: Set<String> = emptySet(),
    val frequencyMinutes: Int = Constants.DEFAULT_FREQUENCY_MINUTES,
    val wallpaperTarget: WallpaperTarget = WallpaperTarget.BOTH,
    val autoChangeEnabled: Boolean = true,
    val cacheSizeMb: Long = 0,
    val isApplying: Boolean = false,
    val isClearing: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val wallpaperRepository: WallpaperRepository,
    private val cacheManager: ImageCacheManager,
    private val fetchWallpaperUseCase: FetchWallpaperUseCase,
    private val smartSelectionUseCase: SmartSelectionUseCase,
    private val applyWallpaperUseCase: ApplyWallpaperUseCase,
    private val scheduleUseCase: ScheduleUseCase,
) : ViewModel() {

    private val _actionState = MutableStateFlow(ActionState())

    data class ActionState(
        val isApplying: Boolean = false,
        val isClearing: Boolean = false,
        val message: String? = null,
    )

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.getSelectedCategories(),
        settingsRepository.getCustomCategories(),
        settingsRepository.getFrequencyMinutes(),
        settingsRepository.getWallpaperTarget(),
        settingsRepository.isAutoChangeEnabled(),
    ) { categories, custom, frequency, target, autoChange ->
        SettingsUiState(
            selectedCategories = categories,
            customCategories = custom,
            frequencyMinutes = frequency,
            wallpaperTarget = target,
            autoChangeEnabled = autoChange,
        )
    }.combine(_actionState) { settings, action ->
        settings.copy(
            cacheSizeMb = cacheManager.getCacheSizeMb(),
            isApplying = action.isApplying,
            isClearing = action.isClearing,
            message = action.message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(),
    )

    fun toggleCategory(category: String) {
        viewModelScope.launch {
            val current = uiState.value.selectedCategories
            val updated = if (category in current) {
                if (current.size > 1) current - category else current
            } else {
                current + category
            }
            settingsRepository.setSelectedCategories(updated)
        }
    }

    fun addCustomCategory(query: String) {
        val trimmed = query.trim().lowercase()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val current = uiState.value
            settingsRepository.setCustomCategories(current.customCategories + trimmed)
            settingsRepository.setSelectedCategories(current.selectedCategories + trimmed)
        }
    }

    fun removeCustomCategory(query: String) {
        viewModelScope.launch {
            val current = uiState.value
            settingsRepository.setCustomCategories(current.customCategories - query)
            settingsRepository.setSelectedCategories(current.selectedCategories - query)
        }
    }

    fun setFrequency(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setFrequencyMinutes(minutes)
            if (uiState.value.autoChangeEnabled) {
                scheduleUseCase.schedule(minutes)
            }
        }
    }

    fun setWallpaperTarget(target: WallpaperTarget) {
        viewModelScope.launch {
            settingsRepository.setWallpaperTarget(target)
        }
    }

    fun setAutoChangeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoChangeEnabled(enabled)
            if (enabled) {
                scheduleUseCase.schedule(uiState.value.frequencyMinutes)
            } else {
                scheduleUseCase.cancel()
            }
        }
    }

    fun applyNow() {
        viewModelScope.launch {
            _actionState.value = ActionState(isApplying = true)
            try {
                val state = uiState.value
                val candidates = fetchWallpaperUseCase(state.selectedCategories)
                if (candidates.isEmpty()) {
                    _actionState.value = ActionState(
                        message = "No wallpapers found. Check your connection.",
                    )
                    return@launch
                }

                val deviceInfo = getDeviceInfo()
                val selected = smartSelectionUseCase(candidates, deviceInfo)
                if (selected != null) {
                    val success = applyWallpaperUseCase(selected, state.wallpaperTarget)
                    _actionState.value = ActionState(
                        message = if (success) "Wallpaper applied!" else "Failed to apply",
                    )
                } else {
                    _actionState.value = ActionState(message = "Could not select a wallpaper")
                }
            } catch (e: Exception) {
                _actionState.value = ActionState(message = "Something went wrong")
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _actionState.value = ActionState(isClearing = true)
            try {
                wallpaperRepository.clearHistory()
                _actionState.value = ActionState(message = "Cache cleared")
            } catch (e: Exception) {
                _actionState.value = ActionState(message = "Failed to clear cache")
            }
        }
    }

    fun clearMessage() {
        _actionState.value = _actionState.value.copy(message = null)
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
