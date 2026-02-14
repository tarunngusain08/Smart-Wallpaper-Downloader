package com.wallshift.app.domain.usecase

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.wallshift.app.util.Constants
import com.wallshift.app.worker.WallpaperChangeWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Manages scheduling and cancelling the periodic wallpaper change worker.
 * For intervals >= 15 min: uses standard PeriodicWorkRequest.
 * For intervals < 15 min: uses chained OneTimeWorkRequest (the Worker
 * re-enqueues itself after completing).
 */
class ScheduleUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    companion object {
        private const val PERIODIC_MIN_MINUTES = 15L
    }

    private val workManager: WorkManager
        get() = WorkManager.getInstance(context)

    /**
     * Schedules a wallpaper change every [intervalMinutes] minutes.
     */
    fun schedule(intervalMinutes: Int) {
        val interval = intervalMinutes.toLong()
            .coerceAtLeast(Constants.MIN_WORK_INTERVAL_MINUTES)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        if (interval >= PERIODIC_MIN_MINUTES) {
            // Cancel any chained work first
            workManager.cancelUniqueWork(Constants.WALLPAPER_CHANGE_CHAIN_TAG)

            val workRequest = PeriodicWorkRequestBuilder<WallpaperChangeWorker>(
                interval, TimeUnit.MINUTES,
            )
                .setConstraints(constraints)
                .addTag(Constants.WALLPAPER_CHANGE_WORK_TAG)
                .build()

            workManager.enqueueUniquePeriodicWork(
                Constants.WALLPAPER_CHANGE_WORK_TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest,
            )
        } else {
            // Cancel any periodic work first
            workManager.cancelUniqueWork(Constants.WALLPAPER_CHANGE_WORK_TAG)

            // Use chained OneTimeWorkRequest for sub-15-minute intervals
            scheduleChainedWork(interval, constraints)
        }
    }

    /**
     * Enqueues a single OneTimeWorkRequest that will fire after [delayMinutes].
     * The Worker itself is responsible for re-enqueuing the next one.
     */
    fun scheduleChainedWork(
        delayMinutes: Long,
        constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build(),
    ) {
        val workRequest = OneTimeWorkRequestBuilder<WallpaperChangeWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag(Constants.WALLPAPER_CHANGE_WORK_TAG)
            .build()

        workManager.enqueueUniqueWork(
            Constants.WALLPAPER_CHANGE_CHAIN_TAG,
            ExistingWorkPolicy.REPLACE,
            workRequest,
        )
    }

    /** Cancels any scheduled wallpaper change work (both periodic and chained). */
    fun cancel() {
        workManager.cancelUniqueWork(Constants.WALLPAPER_CHANGE_WORK_TAG)
        workManager.cancelUniqueWork(Constants.WALLPAPER_CHANGE_CHAIN_TAG)
    }
}
