package com.chesshelper

import android.content.Context
import java.io.File
import java.io.FileOutputStream

class StockfishEngine(private val context: Context) {

    private lateinit var process: Process
    private lateinit var writer: java.io.PrintWriter
    private lateinit var reader: java.io.BufferedReader

    fun start() {
        val stockfishFile = extractBinary()
        stockfishFile.setExecutable(true)

        process = ProcessBuilder(stockfishFile.absolutePath)
            .redirectErrorStream(true)
            .start()

        writer = java.io.PrintWriter(process.outputStream.bufferedWriter())
        reader = process.inputStream.bufferedReader()

        send("uci")
        waitFor("uciok")
        send("isready")
        waitFor("readyok")
    }

    fun getBestMove(fen: String, moveTimeMs: Int = 1000): String {
        send("position fen $fen")
        send("go movetime $moveTimeMs")

        var bestMove = "?"
        while (true) {
            val line = reader.readLine() ?: break
            if (line.startsWith("bestmove")) {
                bestMove = line.split(" ").getOrNull(1) ?: "?"
                break
            }
        }
        return bestMove
    }

    fun stop() {
        send("quit")
        process.destroy()
    }

    private fun send(cmd: String) {
        writer.println(cmd)
        writer.flush()
    }

    private fun waitFor(token: String) {
        while (true) {
            val line = reader.readLine() ?: break
            if (line.contains(token)) break
        }
    }

    private fun extractBinary(): File {
        val outFile = File(context.filesDir, "stockfish")
        if (!outFile.exists()) {
            context.assets.open("stockfish").use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return outFile
    }
}
