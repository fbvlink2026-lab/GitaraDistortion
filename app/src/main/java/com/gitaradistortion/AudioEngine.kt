package com.gitaradistortion

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.util.concurrent.atomic.AtomicBoolean

object AudioEngine {
    private val running = AtomicBoolean(false)
    private var thread: Thread? = null
    private const val SAMPLE_RATE = 44100
    private const val BUF_SIZE = 512

    fun isRunning():Boolean = running.get()

    fun start(ctx: Context) {
        if(running.get()) return
        running.set(true)
        try {
            val minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT)
            val recorder = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT, maxOf(minBuf, BUF_SIZE*2))
            if(recorder.state != AudioRecord.STATE_INITIALIZED) return
            recorder.startRecording()
            thread = Thread {
                val buf = FloatArray(BUF_SIZE)
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)
                while(running.get()) {
                    val n = recorder.read(buf,0,BUF_SIZE, AudioRecord.READ_BLOCKING)
                    if(n>0) for(i in 0 until n) buf[i] = AudioMixer.process(buf[i])
                }
                try{recorder.stop();recorder.release()}catch(_:Exception){}
            }.apply { start() }
        } catch(_:Exception) { running.set(false) }
    }
    fun stop() { running.set(false); thread?.interrupt(); thread=null }
}
