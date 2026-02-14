package com.wallshift.app.domain.usecase

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.wallshift.app.domain.model.WallpaperImage
import com.wallshift.app.domain.model.WallpaperTarget
import com.wallshift.app.domain.repository.WallpaperRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Downloads, processes, and applies a wallpaper image to the device.
 */
class ApplyWallpaperUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wallpaperRepository: WallpaperRepository,
) {

    companion object {
        private const val TAG = "ApplyWallpaperUseCase"
    }

    /**
     * Downloads [image], scales/crops it, and sets it as wallpaper for the given [target].
     * Returns true on success.
     */
    suspend operator fun invoke(
        image: WallpaperImage,
        target: WallpaperTarget,
    ): Boolean {
        // Defensive permission check (SET_WALLPAPER is a normal permission, auto-granted,
        // but some custom ROMs may revoke it)
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.SET_WALLPAPER,
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            Log.w(TAG, "SET_WALLPAPER permission not granted")
            return false
        }

        // Download and cache the image
        val localPath = wallpaperRepository.downloadAndCache(image)
            ?: return false

        // Decode with appropriate sampling to avoid OOM
        val screenSize = getScreenSize()
        val bitmap = decodeSampledBitmap(localPath, screenSize.first, screenSize.second)
            ?: return false

        // Center-crop to device dimensions
        val croppedBitmap = centerCrop(bitmap, screenSize.first, screenSize.second)

        // Apply via WallpaperManager
        val wallpaperManager = WallpaperManager.getInstance(context)
        return try {
            val flag = when (target) {
                WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                WallpaperTarget.BOTH ->
                    WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }

            wallpaperManager.setBitmap(croppedBitmap, null, true, flag)

            // Record in database
            wallpaperRepository.markApplied(image.id)

            // Recycle bitmaps
            if (croppedBitmap !== bitmap) bitmap.recycle()
            croppedBitmap.recycle()

            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getScreenSize(): Pair<Int, Int> {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        return metrics.widthPixels to metrics.heightPixels
    }

    private fun decodeSampledBitmap(
        path: String,
        reqWidth: Int,
        reqHeight: Int,
    ): Bitmap? {
        // First pass: decode bounds only
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, options)

        // Calculate sample size
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(path, options)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun centerCrop(source: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val sourceAspect = source.width.toFloat() / source.height
        val targetAspect = targetWidth.toFloat() / targetHeight

        val scale: Float
        val dx: Float
        val dy: Float

        if (sourceAspect > targetAspect) {
            // Source is wider -- fit height, crop width
            scale = targetHeight.toFloat() / source.height
            dx = (targetWidth - source.width * scale) / 2f
            dy = 0f
        } else {
            // Source is taller -- fit width, crop height
            scale = targetWidth.toFloat() / source.width
            dx = 0f
            dy = (targetHeight - source.height * scale) / 2f
        }

        val matrix = Matrix().apply {
            setRectToRect(
                RectF(0f, 0f, source.width.toFloat(), source.height.toFloat()),
                RectF(dx, dy, dx + source.width * scale, dy + source.height * scale),
                Matrix.ScaleToFit.FILL,
            )
        }

        val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawBitmap(source, matrix, null)
        return output
    }
}
