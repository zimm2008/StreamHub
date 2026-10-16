package com.streamhub.app

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer

class CameraStreamer(private val context: Context) {

    private val TAG = "CameraStreamer"
    private var mediaCodec: MediaCodec? = null
    private var isStreaming = false

    fun getAvailableCameraLenses(): List<Map<String, String>> {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraList = mutableListOf<Map<String, String>>()

        try {
            for (cameraId in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
                
                val lensName = when (lensFacing) {
                    CameraCharacteristics.LENS_FACING_FRONT -> "Front Selfie Camera (ID: $cameraId)"
                    CameraCharacteristics.LENS_FACING_BACK -> "Back Main Camera (ID: $cameraId)"
                    CameraCharacteristics.LENS_FACING_EXTERNAL -> "External Camera (ID: $cameraId)"
                    else -> "Camera ID: $cameraId"
                }

                cameraList.add(mapOf("id" to cameraId, "name" to lensName))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning cameras: ${e.message}")
        }
        return cameraList
    }

    fun startCameraEncoder(width: Int, height: Int, fps: Int, bitrate: Int) {
        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC).apply {
                configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                start()
            }
            isStreaming = true
            Log.d(TAG, "Camera Hardware Encoder started successfully at ${width}x${height} @ ${fps}FPS")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start camera encoder: ${e.message}")
        }
    }

    fun stopCameraEncoder() {
        try {
            isStreaming = false
            mediaCodec?.stop()
            mediaCodec?.release()
            mediaCodec = null
            Log.d(TAG, "Camera Encoder stopped.")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping encoder: ${e.message}")
        }
    }
}
