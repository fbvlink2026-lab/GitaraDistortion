package com.gitaradistortion

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.AudioRecord
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean

object AudioEngine {
    private val running = AtomicBoolean(false)
    private var thread: Thread? = null
    private const val SAMPLE_RATE = 44100
    private const val BUF_SIZE = 512

    fun isRunning(): Boolean = running.get()

    fun start(ctx: Context) {
        if (running.get()) return
        running.set(true)
        try {
            val minRecBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                maxOf(minRecBuf, BUF_SIZE * 4)
            )
            if (recorder.state != AudioRecord.STATE_INITIALIZED) return

            val minTrackBuf = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val track = AudioTrack(
                AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                maxOf(minTrackBuf, BUF_SIZE * 4), AudioTrack.MODE_STREAM
            )

            recorder.startRecording()
            track.play()

            thread = Thread {
                val bufShort = ShortArray(BUF_SIZE)
                val bufOut = ShortArray(BUF_SIZE)
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)
                while (running.get()) {
                    val n = recorder.read(bufShort, 0, BUF_SIZE)
                    if (n > 0) {
                        for (i in 0 until n) {
                            val input = bufShort[i] / 32768.0f
                            val processed = AudioMixer.process(input)
                            val clamped = processed.coerceIn(-1.0f, 1.0f)
                            bufOut[i] = (clamped * 32767.0f).toInt().toShort()
                        }
                        track.write(bufOut, 0, n)
                    }
                }
                try { recorder.stop(); recorder.release() } catch (_: Exception) {}
                try { track.stop(); track.release() } catch (_: Exception) {}
            }.apply { start() }
        } catch (_: Exception) {
            running.set(false)
        }
    }

    fun stop() {
        running.set(false)
        thread?.interrupt()
        thread = null
    }
}
