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
    private const val BUF_SIZE = 512
    private var ctxRef: Context? = null

    fun isRunning()=running.get()

    fun start(ctx:Context):Boolean {
        if(running.get()) return true
        ctxRef = ctx.applicationContext

        // ✅ SURIIN MUNA KUNG MAY PAHINTULOT
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            ctx, android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if(!hasPermission) {
            android.widget.Toast.makeText(ctx, "❌ KAILANGAN NG PAHINTULOT SA MIKROPONO!", android.widget.Toast.LENGTH_SHORT).show()
            return false
        }

        running.set(true)
        try {
            val minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT)
            if(minBuf <= 0) {
                android.widget.Toast.makeText(ctx, "❌ HINDI SUPORTADO ANG AUDIO!", android.widget.Toast.LENGTH_SHORT).show()
                running.set(false)
                return false
            }

            val rec = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT, maxOf(minBuf, BUF_SIZE*2))

            if(rec.state != AudioRecord.STATE_INITIALIZED) {
                android.widget.Toast.makeText(ctx, "❌ HINDI MAKAPAG SIMULA NG AUDIO!", android.widget.Toast.LENGTH_SHORT).show()
                running.set(false)
                return false
            }

            rec.startRecording()
            thread = Thread {
                val buf = FloatArray(BUF_SIZE)
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)
                while(running.get()) {
                    try {
                        val n = rec.read(buf, 0, BUF_SIZE, AudioRecord.READ_BLOCKING)
                        if(n > 0) {
                            for(i in 0 until n) buf[i] = AudioMixer.process(buf[i])
                        }
                    } catch(_:Exception) { break }
                }
                try { rec.stop(); rec.release() } catch(_:Exception) {}
            }.apply { start() }
            return true
        } catch(e:Exception) {
            android.widget.Toast.makeText(ctx, "❌ ERROR: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            running.set(false)
            return false
        }
    }

    fun stop() { running.set(false); thread?.interrupt(); thread=null }
}
