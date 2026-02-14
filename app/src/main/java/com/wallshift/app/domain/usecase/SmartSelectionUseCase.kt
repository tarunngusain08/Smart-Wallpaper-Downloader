package com.wallshift.app.domain.usecase

import com.wallshift.app.domain.model.WallpaperImage
import com.wallshift.app.domain.repository.WallpaperRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.min

/**
 * Scores and selects the best wallpaper candidate based on
 * device resolution fit, aspect ratio match, popularity, freshness,
 * and image quality.
 */
class SmartSelectionUseCase @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
) {

    data class DeviceInfo(
        val screenWidth: Int,
        val screenHeight: Int,
    )

    companion object {
        /** Number of days after which a previously-applied image is considered "fresh" again. */
        private const val FRESHNESS_RECOVERY_DAYS = 30.0
    }

    /**
     * Selects the best wallpaper from [candidates] for the given [deviceInfo].
     * Returns null if [candidates] is empty.
     *
     * Scoring weights:
     *  - Resolution fit:  25%
     *  - Aspect ratio:    25%
     *  - Popularity:      15%
     *  - Freshness:       20%
     *  - Quality:         15%
     */
    suspend operator fun invoke(
        candidates: List<WallpaperImage>,
        deviceInfo: DeviceInfo,
    ): WallpaperImage? {
        if (candidates.isEmpty()) return null

        val appliedTimestamps = wallpaperRepository.getAppliedTimestamps()
        val maxLikes = candidates.maxOf { it.likes }.coerceAtLeast(1)
        val deviceAspect = deviceInfo.screenHeight.toDouble() / deviceInfo.screenWidth
        val devicePixels = deviceInfo.screenWidth.toLong() * deviceInfo.screenHeight
        val now = System.currentTimeMillis()

        val scored = candidates.map { image ->
            val resolutionScore = computeResolutionScore(image, deviceInfo)
            val aspectScore = computeAspectRatioScore(image, deviceAspect)
            val popularityScore = image.likes.toDouble() / maxLikes
            val freshnessScore = computeFreshnessScore(image.id, appliedTimestamps, now)
            val qualityScore = computeQualityScore(image, devicePixels)

            val totalScore = (resolutionScore * 0.25) +
                (aspectScore * 0.25) +
                (popularityScore * 0.15) +
                (freshnessScore * 0.20) +
                (qualityScore * 0.15)

            image to totalScore
        }

        return scored.maxByOrNull { it.second }?.first
    }

    private fun computeResolutionScore(
        image: WallpaperImage,
        deviceInfo: DeviceInfo,
    ): Double {
        val widthRatio = image.width.toDouble() / deviceInfo.screenWidth
        val heightRatio = image.height.toDouble() / deviceInfo.screenHeight
        val minRatio = min(widthRatio, heightRatio)
        // 1.0 if image is at least as large as device, scales down linearly
        return min(minRatio, 1.0)
    }

    private fun computeAspectRatioScore(
        image: WallpaperImage,
        deviceAspect: Double,
    ): Double {
        val imageAspect = image.height.toDouble() / image.width.coerceAtLeast(1)
        val diff = abs(imageAspect - deviceAspect) / deviceAspect
        // 1.0 if perfect match, 0.0 if aspect differs by 100%+
        return (1.0 - diff).coerceIn(0.0, 1.0)
    }

    /**
     * Recency-based freshness score:
     * - 1.0 if never applied
     * - Scales from 0.0 (just applied) to 0.8 over [FRESHNESS_RECOVERY_DAYS] days
     */
    private fun computeFreshnessScore(
        imageId: String,
        appliedTimestamps: Map<String, Long>,
        now: Long,
    ): Double {
        val appliedAt = appliedTimestamps[imageId] ?: return 1.0
        val daysSinceApplied = TimeUnit.MILLISECONDS.toDays(now - appliedAt).toDouble()
        return min(daysSinceApplied / FRESHNESS_RECOVERY_DAYS, 0.8)
    }

    /**
     * Quality score based on resolution surplus: rewards images with
     * significantly higher pixel count than the device (up to 2x),
     * serving as a proxy for sharpness / detail quality.
     */
    private fun computeQualityScore(
        image: WallpaperImage,
        devicePixels: Long,
    ): Double {
        val imagePixels = image.width.toLong() * image.height
        return min(imagePixels.toDouble() / (devicePixels * 2.0), 1.0)
    }
}
