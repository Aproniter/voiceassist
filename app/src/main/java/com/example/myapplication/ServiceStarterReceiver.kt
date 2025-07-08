package com.example.myapplication

import android.app.ActivityManager
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

class ToggleServiceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serviceIntent = Intent(this, AudioRecordService::class.java)

        if (isServiceRunning(AudioRecordService::class.java)) {
            stopService(serviceIntent)
            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show()
        } else {
            startForegroundService(serviceIntent)
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}