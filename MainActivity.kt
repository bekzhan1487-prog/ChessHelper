package com.chesshelper

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var projectionManager: MediaProjectionManager
    private val REQUEST_PROJECTION = 1001
    private val REQUEST_OVERLAY = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            checkOverlayAndStart()
        }
    }

    override fun onResume() {
        super.onResume()
        // Когда возвращаемся из настроек — проверяем разрешение
        if (Settings.canDrawOverlays(this)) {
            val btn = findViewById<Button>(R.id.btnStart)
            btn.text = "Запустить (разрешение есть)"
        }
    }

    private fun checkOverlayAndStart() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Выдайте разрешение и нажмите Запустить снова", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, REQUEST_OVERLAY)
        } else {
            requestProjection()
        }
    }

    private fun requestProjection() {
        startActivityForResult(
            projectionManager.createScreenCaptureIntent(),
            REQUEST_PROJECTION
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_OVERLAY -> {
                if (Settings.canDrawOverlays(this)) {
                    requestProjection()
                } else {
                    Toast.makeText(this, "Разрешение не выдано!", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_PROJECTION -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val intent = Intent(this, OverlayService::class.java).apply {
                        putExtra("resultCode", resultCode)
                        putExtra("data", data)
                    }
                    startForegroundService(intent)
                    Toast.makeText(this, "ChessHelper запущен!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Разрешение на захват экрана не выдано!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
