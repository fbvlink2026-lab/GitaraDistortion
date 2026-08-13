package com.gitaradistortion

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import java.util.concurrent.atomic.AtomicBoolean

object AudioEngine {
    private val running = AtomicBoolean(false)
    private var thread: Thread? = null
    private const val SAMPLE_RATE = 48000
    private const val BUF_SIZE = 1024

    fun start(ctx: Context) {
        if(running.get()) return
        running.set(true)

        val minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT)
        val bufSize = maxOf(minBuf, BUF_SIZE * 4)

        try {
            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_FLOAT,
                bufSize
            )

            recorder.startRecording()

            thread = Thread {
                val buffer = FloatArray(BUF_SIZE)
                while(running.get()) {
                    val n = recorder.read(buffer, 0, BUF_SIZE, AudioRecord.READ_BLOCKING)
                    if(n > 0) {
                        for(i in 0 until n) {
                            buffer[i] = AudioMixer.process(buffer[i])
                        }
                        processOutput(buffer, n)
                    }
                }
                recorder.stop()
                recorder.release()
            }.apply { start() }

        } catch(e: Exception) {
            Handler(Looper.getMainLooper()).post {
                android.widget.Toast.makeText(ctx, "❌ Audio Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    fun stop() {
        running.set(false)
        thread?.interrupt()
        thread = null
    }

    private fun processOutput(buffer:FloatArray, n:Int) {
        // ✅ Ipadala sa native output o Oboe stream
        sendToNative(buffer, n)
    }

    private external fun sendToNative(buffer:FloatArray, n:Int)
}
