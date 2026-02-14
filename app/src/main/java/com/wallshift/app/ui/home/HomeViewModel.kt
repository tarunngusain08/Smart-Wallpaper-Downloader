package com.wallshift.app.ui.home

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallshift.app.data.db.WallpaperHistoryEntity
import com.wallshift.app.domain.model.WallpaperTarget
import com.wallshift.app.domain.repository.SettingsRepository
import com.wallshift.app.domain.repository.WallpaperRepository
import com.wallshift.app.domain.usecase.ApplyWallpaperUseCase
import com.wallshift.app.domain.usecase.FetchWallpaperUseCase
import com.wallshift.app.domain.usecase.SmartSelectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class HomeUiState(
    val currentWallpaper: WallpaperHistoryEntity? = null,
    val isAutoChangeEnabled: Boolean = true,
    val frequencyMinutes: Int = 360,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wallpaperRepository: WallpaperRepository,
    private val settingsRepository: SettingsRepository,
    private val fetchWallpaperUseCase: FetchWallpaperUseCase,
    private val smartSelectionUseCase: SmartSelectionUseCase,
    private val applyWallpaperUseCase: ApplyWallpaperUseCase,
) : ViewModel() {

    private val _actionState = MutableStateFlow(ActionState())
    val actionState: StateFlow<ActionState> = _actionState.asStateFlow()

    data class ActionState(
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val error: String? = null,
        val saveSuccess: Boolean = false,
    )

    val uiState: StateFlow<HomeUiState> = combine(
        wallpaperRepository.observeCurrentWallpaper(),
        settingsRepository.isAutoChangeEnabled(),
        settingsRepository.getFrequencyMinutes(),
        _actionState,
    ) { wallpaper, autoChange, frequency, action ->
        HomeUiState(
            currentWallpaper = wallpaper,
            isAutoChangeEnabled = autoChange,
            frequencyMinutes = frequency,
            isLoading = action.isLoading,
            isRefreshing = action.isRefreshing,
            error = action.error,
            saveSuccess = action.saveSuccess,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(),
    )

    /** Skip to the next wallpaper immediately. */
    fun nextWallpaper() {
        viewModelScope.launch {
            _actionState.value = ActionState(isRefreshing = true)
            try {
                val categories = settingsRepository.getSelectedCategories()
                    .stateIn(viewModelScope).value
                val target = settingsRepository.getWallpaperTarget()
                    .stateIn(viewModelScope).value

                val candidates = fetchWallpaperUseCase(categories)
                if (candidates.isEmpty()) {
                    _actionState.value = ActionState(
                        error = "No wallpapers found. Check your internet connection.",
                    )
                    return@launch
                }

                val deviceInfo = getDeviceInfo()
                val selected = smartSelectionUseCase(candidates, deviceInfo)
                if (selected != null) {
                    val success = applyWallpaperUseCase(selected, target)
                    _actionState.value = if (success) {
                        ActionState()
                    } else {
                        ActionState(error = "Failed to apply wallpaper")
                    }
                } else {
                    _actionState.value = ActionState(error = "Could not select a wallpaper")
                }
            } catch (e: Exception) {
                _actionState.value = ActionState(error = "Something went wrong")
            }
        }
    }

    /** Save the current wallpaper to the device's Pictures directory. */
    fun saveCurrentWallpaper() {
        viewModelScope.launch {
            val wallpaper = uiState.value.currentWallpaper ?: return@launch
            val localPath = wallpaper.localPath ?: return@launch

            try {
                val file = File(localPath)
                if (!file.exists()) {
                    _actionState.value = _actionState.value.copy(
                        error = "Cached file not found",
                    )
                    return@launch
                }

                val bitmap = BitmapFactory.decodeFile(localPath) ?: return@launch
                val fileName = "WallShift_${wallpaper.id}.jpg"

                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/WallShift",
                    )
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues,
                )

                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(
                            android.graphics.Bitmap.CompressFormat.JPEG,
                            95,
                            outputStream,
                        )
                    }
                }

                bitmap.recycle()

                _actionState.value = _actionState.value.copy(
                    saveSuccess = true,
                    error = null,
                )
            } catch (e: Exception) {
                _actionState.value = _actionState.value.copy(
                    error = "Failed to save wallpaper",
                )
            }
        }
    }

    fun clearError() {
        _actionState.value = _actionState.value.copy(error = null)
    }

    fun clearSaveSuccess() {
        _actionState.value = _actionState.value.copy(saveSuccess = false)
    }

    /**
     * Re-applies the current wallpaper with a user-defined crop adjustment.
     * [offsetX] / [offsetY] are pixel translations, [scale] is the zoom factor.
     */
    fun reapplyWithCrop(offsetX: Float, offsetY: Float, scale: Float) {
        viewModelScope.launch {
            val wallpaper = uiState.value.currentWallpaper ?: return@launch
            val localPath = wallpaper.localPath ?: return@launch

            _actionState.value = _actionState.value.copy(isLoading = true)
            try {
                val (screenW, screenH) = getScreenSize()
                val success = withContext(Dispatchers.IO) {
                    val bitmap = BitmapFactory.decodeFile(localPath) ?: return@withContext false

                    // Build a matrix that applies the user's zoom + pan
                    val baseScale = screenH.toFloat() / bitmap.height
                    val matrix = Matrix().apply {
                        // Scale the image so it fills the screen height
                        postScale(baseScale * scale, baseScale * scale)
                        postTranslate(offsetX, offsetY)
                    }

                    val output = Bitmap.createBitmap(screenW, screenH, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(output)
                    canvas.drawBitmap(bitmap, matrix, null)
                    bitmap.recycle()

                    val target = settingsRepository.getWallpaperTarget()
                        .firstOrNull() ?: WallpaperTarget.BOTH
                    val flag = when (target) {
                        WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                        WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                        WallpaperTarget.BOTH ->
                            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                    }

                    val wm = WallpaperManager.getInstance(context)
                    wm.setBitmap(output, null, true, flag)
                    output.recycle()
                    true
                }

                _actionState.value = if (success) {
                    _actionState.value.copy(isLoading = false, error = null)
                } else {
                    _actionState.value.copy(
                        isLoading = false,
                        error = "Failed to adjust wallpaper",
                    )
                }
            } catch (e: Exception) {
                _actionState.value = _actionState.value.copy(
                    isLoading = false,
                    error = "Failed to adjust wallpaper",
                )
            }
        }
    }

    private fun getScreenSize(): Pair<Int, Int> {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        return metrics.widthPixels to metrics.heightPixels
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
