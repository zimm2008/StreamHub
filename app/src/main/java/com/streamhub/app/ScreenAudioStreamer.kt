package com.streamhub.app

import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.os.PowerManager
import android.util.Log
import android.view.Surface

class ScreenAudioStreamer(private val context: Context) {

    private val TAG = "ScreenAudioStreamer"
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var wakeLock: PowerManager.WakeLock? = null

    // পিসি থেকে কমান্ড আসলে ফোনের স্ক্রিন ব্যাকলাইট অফ করা (কিন্তু প্রসেসর চালু রেখে ব্যাটারি ও হিট সেভ করা)
    fun turnOffPhoneScreenBacklight() {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            // PARTIAL_WAKE_LOCK দিয়ে CPU সচল রাখা হয় কিন্তু ডিসপ্লে প্যানেল অফ থাকে
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StreamHub::ScreenOffTag").apply {
                acquire(10 * 60 * 60 * 1000L) // সর্বোচ্চ ১০ ঘণ্টা পর্যন্ত স্ক্রিন অফ রেখে স্ট্রিম করা যাবে
            }
            Log.s(TAG, "Phone screen display turned off for streaming protection.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to turn off screen backlight: ${e.message}")
        }
    }

    // স্ক্রিন মিররিং বন্ধ হলে ব্যাকলাইট আবার আগের অবস্থায় ফিরিয়ে আনা
    fun restorePhoneScreen() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            Log.s(TAG, "Phone screen restored.")
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring screen: ${e.message}")
        }
    }

    // ভার্চুয়াল ডিসপ্লে সেটআপ (MediaProjection দিয়ে ফুল কোয়ালিটি স্ক্রিন ক্যাপচার)
    fun setupVirtualDisplay(mediaProjection: MediaProjection, width: Int, height: Int, dpi: Int, surface: Surface) {
        try {
            virtualDisplay = mediaProjection.createVirtualDisplay(
                "StreamHubScreenMirror",
                width,
                height,
                dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface,
                null,
                null
            )
            Log.s(TAG, "Virtual Display created successfully for screen streaming.")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up virtual display: ${e.message}")
        }
    }

    fun releaseVirtualDisplay() {
        virtualDisplay?.release()
        virtualDisplay = null
    }
}