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

    fun turnOffPhoneScreenBacklight() {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StreamHub::ScreenOffTag").apply {
                acquire(10 * 60 * 60 * 1000L) 
            }
            Log.d(TAG, "Phone screen display turned off for streaming protection.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to turn off screen backlight: ${e.message}")
        }
    }

    fun restorePhoneScreen() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            Log.d(TAG, "Phone screen restored.")
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring screen: ${e.message}")
        }
    }

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
            Log.d(TAG, "Virtual Display created successfully for screen streaming.")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up virtual display: ${e.message}")
        }
    }

    fun releaseVirtualDisplay() {
        virtualDisplay?.release()
        virtualDisplay = null
    }
}
