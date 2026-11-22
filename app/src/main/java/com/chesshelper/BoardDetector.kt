package com.chesshelper

import android.graphics.Bitmap
import android.graphics.Color

object BoardDetector {

    private val LIGHT_COLOR = Color.rgb(240, 217, 181)
    private val DARK_COLOR  = Color.rgb(181, 136, 99)
    private const val COLOR_TOLERANCE = 30

    data class DetectedBoard(
        val bitmap: Bitmap,
        val startX: Int,
        val startY: Int,
        val cellSize: Int
    )

    fun detect(screen: Bitmap): DetectedBoard? {
        val w = screen.width
        val h = screen.height

        val scanY = h / 2

        var boardLeft = -1
        var boardRight = -1
        var runStart = -1
        var runCount = 0

        for (x in 0 until w) {
            val pixel = screen.getPixel(x, scanY)
            if (isChessColor(pixel)) {
                if (runStart < 0) runStart = x
                runCount++
            } else {
                if (runCount > 100) {
                    boardLeft = runStart
                    boardRight = runStart + runCount
                }
                runStart = -1
                runCount = 0
            }
        }
        if (runCount > 100) {
            boardLeft = runStart
            boardRight = runStart + runCount
        }

        if (boardLeft < 0) return null

        val boardSize = boardRight - boardLeft
        if (boardSize < 200) return null

        val scanX = boardLeft + boardSize / 2
        var boardTop = -1
        var boardBottom = -1

        for (y in 0 until h) {
            val pixel = screen.getPixel(scanX, y)
            if (isChessColor(pixel)) {
                if (boardTop < 0) boardTop = y
                boardBottom = y
            }
        }

        if (boardTop < 0) return null

        val boardHeight = boardBottom - boardTop
        val side = minOf(boardSize, boardHeight)
        val cellSize = side / 8

        val cropped = Bitmap.createBitmap(screen, boardLeft, boardTop, side, side)
        val scaled = Bitmap.createScaledBitmap(cropped, 512, 512, true)

        return DetectedBoard(scaled, boardLeft, boardTop, 512 / 8)
    }

    private fun isChessColor(pixel: Int): Boolean {
        return colorClose(pixel, LIGHT_COLOR) || colorClose(pixel, DARK_COLOR)
    }

    private fun colorClose(a: Int, b: Int): Boolean {
        val dr = Math.abs(Color.red(a) - Color.red(b))
        val dg = Math.abs(Color.green(a) - Color.green(b))
        val db = Math.abs(Color.blue(a) - Color.blue(b))
        return dr < COLOR_TOLERANCE && dg < COLOR_TOLERANCE && db < COLOR_TOLERANCE
    }
}
