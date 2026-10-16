package com.streamhub.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class StreamService : Service() {

    private val CHANNEL_ID = "StreamHubServiceChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // অ্যান্ড্রয়েড রুল অনুযায়ী ফোরগ্রাউন্ড সার্ভিস নোটিফিকেশন তৈরি করা
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("StreamHub Active")
            .setContentText("Waiting for PC connection (Wired / Wireless)...")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)

        // এখানে পিসির সাথে সকেট বা ADB কানেকশন লিসেনিং ব্যাকগ্রাউন্ডে রান করবে
        initializeSocketServer()

        return START_STICKY
    }

    private fun initializeSocketServer() {
        // ব্যাকগ্রাউন্ড থ্রেডে পিসির সাথে কমিউনিকেশন হ্যান্ডেল করার লজিক
        Thread {
            try {
                // পিসি থেকে কানেকশন আসার জন্য সকেট পোর্ট ওপেন রাখা (যেমন পোর্ট 27183)
                // পরবর্তী ধাপে ক্যামেরা ও স্ক্রিন স্ট্রিমার এখানে কানেক্ট হবে
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "StreamHub Background Service Channel",
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
        // সার্ভিস বন্ধ হলে রিসোর্স ক্লিনআপ
    }
}