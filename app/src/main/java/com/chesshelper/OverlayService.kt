package com.chesshelper

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var tvMove: TextView
    private lateinit var btnWhite: Button
    private lateinit var btnBlack: Button
    private lateinit var screenCapture: ScreenCaptureService
    private lateinit var stockfish: StockfishEngine

    private var playerColor = "white"
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        setupOverlay()
        stockfish = StockfishEngine(this)
        stockfish.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("resultCode", -1) ?: return START_NOT_STICKY
        val data = intent.getParcelableExtra<Intent>("data") ?: return START_NOT_STICKY

        screenCapture = ScreenCaptureService(this, resultCode, data)
        startAnalysisLoop()
        return START_STICKY
    }

    private fun setupOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay, null)
        tvMove = overlayView.findViewById(R.id.tvMove)
        btnWhite = overlayView.findViewById(R.id.btnWhite)
        btnBlack = overlayView.findViewById(R.id.btnBlack)

        btnWhite.setOnClickListener {
            playerColor = "white"
            btnWhite.setBackgroundColor(Color.GREEN)
            btnBlack.setBackgroundColor(Color.GRAY)
        }
        btnBlack.setOnClickListener {
            playerColor = "black"
            btnBlack.setBackgroundColor(Color.GREEN)
            btnWhite.setBackgroundColor(Color.GRAY)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }

        var initialX = 0; var initialY = 0
        var initialTouchX = 0f; var initialTouchY = 0f
        overlayView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x; initialY = params.y
                    initialTouchX = event.rawX; initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(overlayView, params)
                    true
                }
                else -> false
            }
        }

        windowManager.addView(overlayView, params)
    }

    private fun startAnalysisLoop() {
        scope.launch {
            while (isActive) {
                try {
                    val bitmap = screenCapture.capture()
                    val board = BoardDetector.detect(bitmap)
                    if (board != null) {
                        val fen = FenBuilder.build(board, playerColor)
                        val move = stockfish.getBestMove(fen)
                        tvMove.text = move
                    }
                } catch (e: Exception) {
                    // пропускаем кадр
                }
                delay(1500)
            }
        }
    }

    private fun startForegroundNotification() {
        val channelId = "chess_helper"
        val channel = NotificationChannel(
            channelId, "ChessHelper",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("ChessHelper активен")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        scope.cancel()
        stockfish.stop()
        windowManager.removeView(overlayView)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
