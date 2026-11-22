package com.chesshelper

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.DisplayMetrics

class ScreenCaptureService(
    private val context: Context,
    resultCode: Int,
    data: Intent
) {
    private val projection: MediaProjection
    private val imageReader: ImageReader
    private val virtualDisplay: VirtualDisplay
    private val metrics: DisplayMetrics = context.resources.displayMetrics

    init {
        val manager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE)
                as MediaProjectionManager
        projection = manager.getMediaProjection(resultCode, data)

        imageReader = ImageReader.newInstance(
            metrics.widthPixels, metrics.heightPixels,
            PixelFormat.RGBA_8888, 2
        )

        virtualDisplay = projection.createVirtualDisplay(
            "ChessCapture",
            metrics.widthPixels, metrics.heightPixels, metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface, null, null
        )
    }

    fun capture(): Bitmap {
        val image = imageReader.acquireLatestImage()
            ?: throw Exception("No image available")

        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * metrics.widthPixels

        val bitmap = Bitmap.createBitmap(
            metrics.widthPixels + rowPadding / pixelStride,
            metrics.heightPixels,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        image.close()

        return Bitmap.createBitmap(bitmap, 0, 0, metrics.widthPixels, metrics.heightPixels)
    }

    fun stop() {
        virtualDisplay.release()
        projection.stop()
    }
}
