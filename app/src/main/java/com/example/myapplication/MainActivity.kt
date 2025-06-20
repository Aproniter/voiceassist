package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (!granted) {
            Toast.makeText(this, "Permissions required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        requestPermissionsLauncher.launch(permissions.toTypedArray())


        val startButton = findViewById<Button>(R.id.btnStart)
        val stopButton = findViewById<Button>(R.id.btnStop)

        startButton.setOnClickListener {
            val intent = Intent(this, AudioRecordService::class.java)
            startForegroundService(intent)
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
        }

        stopButton.setOnClickListener {
            val intent = Intent(this, AudioRecordService::class.java)
            stopService(intent)
            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show()
        }
    }
}