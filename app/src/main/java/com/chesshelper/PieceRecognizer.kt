package com.chesshelper

import android.graphics.Bitmap
import android.graphics.Color

object PieceRecognizer {

    private const val WHITE_THRESHOLD = 200
    private const val BLACK_THRESHOLD = 80
    private const val PIECE_PIXEL_MIN = 15

    fun recognize(cellBitmap: Bitmap, isDarkSquare: Boolean): Char {
        val w = cellBitmap.width
        val h = cellBitmap.height

        val x0 = (w * 0.3).toInt()
        val x1 = (w * 0.7).toInt()
        val y0 = (h * 0.3).toInt()
        val y1 = (h * 0.7).toInt()

        val bgLight = intArrayOf(240, 217, 181)
        val bgDark  = intArrayOf(181, 136, 99)
        val bg = if (isDarkSquare) bgDark else bgLight

        var whitePx = 0
        var blackPx = 0

        for (y in y0 until y1) {
            for (x in x0 until x1) {
                val pixel = cellBitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                val dr = Math.abs(r - bg[0])
                val dg = Math.abs(g - bg[1])
                val db = Math.abs(b - bg[2])
                if (dr + dg + db < 40) continue

                val brightness = (r * 299 + g * 587 + b * 114) / 1000
                when {
                    brightness > WHITE_THRESHOLD -> whitePx++
                    brightness < BLACK_THRESHOLD -> blackPx++
                }
            }
        }

        if (whitePx < PIECE_PIXEL_MIN && blackPx < PIECE_PIXEL_MIN) return '.'

        return if (whitePx > blackPx) 'P' else 'p'
    }
}
