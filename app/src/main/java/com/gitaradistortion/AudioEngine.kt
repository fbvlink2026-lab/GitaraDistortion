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
    private const val SAMPLE_RATE = 44100
    private const val BUF_SIZE = 1024

    fun start(ctx: Context) {
        if(running.get()) return
        running.set(true)

        try {
            val minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT)
            val bufSize = maxOf(minBuf, BUF_SIZE * 4)

            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_FLOAT,
                bufSize
            )

            if(recorder.state != AudioRecord.STATE_INITIALIZED) {
                Handler(Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(ctx, "❌ Hindi mabuksan ang Mikropono!", android.widget.Toast.LENGTH_SHORT).show()
                }
                return
            }

            recorder.startRecording()

            thread = Thread {
                val buffer = FloatArray(BUF_SIZE)
                while(running.get()) {
                    try {
                        val n = recorder.read(buffer, 0, BUF_SIZE, AudioRecord.READ_BLOCKING)
                        if(n > 0) {
                            for(i in 0 until n) {
                                buffer[i] = AudioMixer.process(buffer[i])
                            }
                        }
                    } catch (e: Exception) {
                        break
                    }
                }
                try { recorder.stop() } catch(_: Exception) {}
                try { recorder.release() } catch(_: Exception) {}
            }.apply { 
                priority = Thread.MAX_PRIORITY
                start() 
            }

        } catch(e: Exception) {
            running.set(false)
            Handler(Looper.getMainLooper()).post {
                android.widget.Toast.makeText(ctx, "❌ Audio Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    fun stop() {
        running.set(false)
        thread?.interrupt()
        try { thread?.join(500) } catch(_: Exception) {}
        thread = null
    }
}
