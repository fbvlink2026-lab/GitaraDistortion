package com.gitaradistortion

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
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
            val minBuf = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT)
            val recorder = android.media.AudioRecord(
                MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT,
                maxOf(minBuf, BUF_SIZE*2)
            )
            if(recorder.state != android.media.AudioRecord.STATE_INITIALIZED) return
            val track = AudioTrack.Builder()
                .setAudioAttributes(android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(AudioFormat.Builder().setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT).build())
                .setBufferSizeInBytes(maxOf(minBuf, BUF_SIZE*2))
                .setTransferMode(AudioTrack.MODE_BLOCKING).build()
            recorder.startRecording()
            track.play()
            thread = Thread {
                val buf = FloatArray(BUF_SIZE)
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)
                while(running.get()) {
                    val n = recorder.read(buf, 0, BUF_SIZE, android.media.AudioRecord.READ_BLOCKING)
                    if(n>0) {
                        for(i in 0 until n) buf[i] = AudioMixer.process(buf[i])
                        track.write(buf, 0, n, AudioTrack.WRITE_BLOCKING)
                    }
                }
                try{recorder.stop();recorder.release()}catch(_:Exception){}
                try{track.stop();track.release()}catch(_:Exception){}
            }.apply { start() }
        } catch(_:Exception) { running.set(false) }
    }
    fun stop() { running.set(false); thread?.interrupt(); thread=null }
}
