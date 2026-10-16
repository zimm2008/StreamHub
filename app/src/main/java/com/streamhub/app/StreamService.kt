package com.streamhub.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.net.Socket

class StreamService : Service() {

    private val CHANNEL_ID = "StreamHubServiceChannel"
    private val TAG = "StreamService"
    private var isServiceRunning = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isServiceRunning) return START_STICKY
        isServiceRunning = true

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("StreamHub Active")
            .setContentText("Connected & Ready for PC...")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        try {
            // Android 14/13 এর কড়া সিকিউরিটি ক্র্যাশ বাইপাস করার জন্য dataSync টাইপ ব্যবহার
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(1, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Foreground error: ${e.message}")
            startForeground(1, notification)
        }

        initializeSocketConnection()

        return START_STICKY
    }

    private fun initializeSocketConnection() {
        Thread {
            try {
                // USB (ADB Reverse) এর মাধ্যমে পিসির পোর্ট 27183-এ সরাসরি কানেক্ট হওয়া
                val socket = Socket("127.0.0.1", 27183)
                Log.d(TAG, "Connected to PC via USB Socket!")
                
                // এখানে লাইভ ডেটা লুপ চলবে
                while(isServiceRunning) {
                    Thread.sleep(1000)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Socket Connection Error: ${e.message}")
            }
        }.start()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "StreamHub Background Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
    }
}
