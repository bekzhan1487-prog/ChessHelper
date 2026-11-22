package com.chesshelper

import android.graphics.Bitmap

object FenBuilder {

    fun build(board: BoardDetector.DetectedBoard, playerColor: String): String {
        val bitmap = board.bitmap
        val cellSize = board.cellSize
        val grid = Array(8) { CharArray(8) { '.' } }

        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val x = col * cellSize
                val y = row * cellSize
                val cell = Bitmap.createBitmap(bitmap, x, y, cellSize, cellSize)
                val isDark = (row + col) % 2 == 1
                grid[row][col] = PieceRecognizer.recognize(cell, isDark)
            }
        }

        val finalGrid = if (playerColor == "black") {
            Array(8) { r -> CharArray(8) { c -> grid[7 - r][7 - c] } }
        } else grid

        val sb = StringBuilder()
        for (row in 0 until 8) {
            var empty = 0
            for (col in 0 until 8) {
                val piece = finalGrid[row][col]
                if (piece == '.') {
                    empty++
                } else {
                    if (empty > 0) { sb.append(empty); empty = 0 }
                    sb.append(piece)
                }
            }
            if (empty > 0) sb.append(empty)
            if (row < 7) sb.append('/')
        }

        val turn = if (playerColor == "white") "w" else "b"
        sb.append(" $turn KQkq - 0 1")

        return sb.toString()
    }
}
