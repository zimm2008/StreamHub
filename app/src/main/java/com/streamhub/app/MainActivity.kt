package com.streamhub.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // সুন্দর একটি ডার্ক থিম ড্যাশবোর্ড লেআউট তৈরি করা
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(0xFF121212.toInt())
            setPadding(40, 40, 40, 40)
        }

        statusText = TextView(this).apply {
            text = "StreamHub: Ready\nWaiting for PC Connection..."
            setTextColor(0xFF00E676.toInt()) // ব্রাইট গ্রিন কালার
            textSize = 20f
            gravity = android.view.Gravity.CENTER
        }

        layout.addView(statusText)
        setContentView(layout)

        // ক্যামেরা ও অডিও পারমিশন চেক ও রিকোয়েস্ট করা
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val listPermissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            startBackgroundStreamService()
        }
    }

    private fun startBackgroundStreamService() {
        val serviceIntent = Intent(this, StreamService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                statusText.text = "StreamHub: Connected & Ready\nWaiting for PC..."
                startBackgroundStreamService()
            } else {
                statusText.text = "Error: Camera & Mic Permissions Required!"
                statusText.setTextColor(0xFFFF5252.toInt())
            }
        }
    }
}
