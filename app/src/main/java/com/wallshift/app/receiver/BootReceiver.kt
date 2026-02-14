package com.wallshift.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.wallshift.app.domain.repository.SettingsRepository
import com.wallshift.app.domain.usecase.ScheduleUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Re-schedules the wallpaper change worker after a device reboot
 * if auto-change is enabled.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var scheduleUseCase: ScheduleUseCase

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.d(TAG, "Boot completed, checking auto-change settings")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val autoChangeEnabled = settingsRepository.isAutoChangeEnabled()
                    .firstOrNull() ?: false
                val frequencyMinutes = settingsRepository.getFrequencyMinutes()
                    .firstOrNull() ?: return@launch

                if (autoChangeEnabled) {
                    scheduleUseCase.schedule(frequencyMinutes)
                    Log.d(TAG, "Re-scheduled wallpaper changes every $frequencyMinutes min")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to re-schedule after boot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
