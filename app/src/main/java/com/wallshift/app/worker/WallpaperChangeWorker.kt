package com.wallshift.app.worker

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wallshift.app.domain.model.WallpaperTarget
import com.wallshift.app.domain.repository.SettingsRepository
import com.wallshift.app.domain.usecase.ApplyWallpaperUseCase
import com.wallshift.app.domain.usecase.FetchWallpaperUseCase
import com.wallshift.app.domain.usecase.ScheduleUseCase
import com.wallshift.app.domain.usecase.SmartSelectionUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

/**
 * Background worker that fetches a new wallpaper, selects the best candidate,
 * and applies it to the device. Scheduled via WorkManager.
 *
 * For sub-15-minute intervals, this worker re-enqueues itself via
 * [ScheduleUseCase.scheduleChainedWork] after each run.
 */
@HiltWorker
class WallpaperChangeWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val fetchWallpaperUseCase: FetchWallpaperUseCase,
    private val smartSelectionUseCase: SmartSelectionUseCase,
    private val applyWallpaperUseCase: ApplyWallpaperUseCase,
    private val settingsRepository: SettingsRepository,
    private val scheduleUseCase: ScheduleUseCase,
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "WallpaperChangeWorker"
        private const val PERIODIC_MIN_MINUTES = 15L
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting wallpaper change work")

        val result = try {
            // 1. Read current settings
            val categories = settingsRepository.getSelectedCategories().firstOrNull()
            if (categories.isNullOrEmpty()) {
                Log.w(TAG, "No categories selected, skipping")
                return Result.success()
            }

            val target = settingsRepository.getWallpaperTarget().firstOrNull()
                ?: WallpaperTarget.BOTH

            // 2. Fetch candidates
            val candidates = fetchWallpaperUseCase(categories)
            if (candidates.isEmpty()) {
                Log.w(TAG, "No wallpaper candidates found")
                return Result.retry()
            }

            // 3. Smart select the best one
            val deviceInfo = getDeviceInfo()
            val selected = smartSelectionUseCase(candidates, deviceInfo)
            if (selected == null) {
                Log.w(TAG, "Smart selection returned null")
                return Result.retry()
            }

            Log.d(TAG, "Selected wallpaper: ${selected.id} from ${selected.source}")

            // 4. Apply it
            val success = applyWallpaperUseCase(selected, target)
            if (success) {
                Log.d(TAG, "Wallpaper applied successfully")
                Result.success()
            } else {
                Log.e(TAG, "Failed to apply wallpaper")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Worker failed", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }

        // Re-enqueue for chained scheduling (sub-15-min intervals)
        reEnqueueIfNeeded()

        return result
    }

    /**
     * If the user's chosen frequency is below 15 minutes, re-schedule
     * the next chained OneTimeWorkRequest.
     */
    private suspend fun reEnqueueIfNeeded() {
        try {
            val autoEnabled = settingsRepository.isAutoChangeEnabled()
                .firstOrNull() ?: false
            if (!autoEnabled) return

            val frequencyMinutes = settingsRepository.getFrequencyMinutes()
                .firstOrNull()?.toLong() ?: return

            if (frequencyMinutes < PERIODIC_MIN_MINUTES) {
                Log.d(TAG, "Re-enqueuing chained work for ${frequencyMinutes}min")
                scheduleUseCase.scheduleChainedWork(frequencyMinutes)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to re-enqueue chained work", e)
        }
    }

    private fun getDeviceInfo(): SmartSelectionUseCase.DeviceInfo {
        val wm = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        return SmartSelectionUseCase.DeviceInfo(
            screenWidth = metrics.widthPixels,
            screenHeight = metrics.heightPixels,
        )
    }
}
