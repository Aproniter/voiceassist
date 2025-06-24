package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class StartServiceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serviceIntent = Intent(this, AudioRecordService::class.java)
        startForegroundService(serviceIntent)
        Toast.makeText(this, "Recording started from Activity", Toast.LENGTH_SHORT).show()

        finish()
    }
}

class StopServiceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serviceIntent = Intent(this, AudioRecordService::class.java)
        stopService(serviceIntent)
        Toast.makeText(this, "Recording stoped from Activity", Toast.LENGTH_SHORT).show()

        finish()
    }
}