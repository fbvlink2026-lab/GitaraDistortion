package com.gitaradistortion

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.widget.Toast

object AudioEngine {
    private var recorder: AudioRecord? = null
    private var isRunning = false
    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_FLOAT
    )

    fun start(context: Context) {
        if (isRunning) return
        try {
            recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_FLOAT,
                bufferSize * 4
            )

            recorder?.startRecording()
            isRunning = true

            // ✅ LALABAS ANG MENSAHE SA SCREEN!
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "🎸 Ginagamit ang mikropono — Handa na ang Gitara!", Toast.LENGTH_LONG).show()
            }

            Thread { processAudioLoop() }.start()
        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "❌ Hindi mabuksan ang mikropono!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun processAudioLoop() {
        val buffer = FloatArray(bufferSize)
        while (isRunning) {
            val read = recorder?.read(buffer, 0, buffer.size, AudioRecord.READ_BLOCKING) ?: -1
            if (read > 0) {
                for (i in 0 until read) {
                    // ✅ IPASA SA MIXER — PROSESO → BALIK SA SPEAKER!
                    buffer[i] = AudioMixer.process(buffer[i])
                }
            }
        }
    }

    fun stop() {
        isRunning = false
        recorder?.stop()
        recorder?.release()
        recorder = null
    }
}
