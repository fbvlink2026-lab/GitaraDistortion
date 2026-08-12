package com.gitaradistortion

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.widget.Toast

object AudioEngine {
    private var recorder: AudioRecord? = null
    private var track: AudioTrack? = null
    private var isRunning = false
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_OUT_MONO
    private val inChannelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_FLOAT
    private var bufferSize = 0

    fun start(context: Context) {
        if (isRunning) return
        try {
            bufferSize = AudioRecord.getMinBufferSize(sampleRate, inChannelConfig, audioFormat)
            if (bufferSize < 512) bufferSize = 1024

            // ✅ AUDIO INPUT — BASAHIN ANG GITARA
            recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                inChannelConfig,
                audioFormat,
                bufferSize * 4
            )

            // ✅ AUDIO OUTPUT — MODE_STREAM NA! SIGURADONG KILALA!
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(audioFormat)
                        .setChannelMask(channelConfig)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 4)
                .setTransferMode(AudioTrack.MODE_STREAM) // ✅ TAMA NA!
                .build()

            recorder?.startRecording()
            track?.play()
            isRunning = true

            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "✅ Handa na! Gitara → App → Speaker!", Toast.LENGTH_LONG).show()
            }

            Thread { processLoop() }.start()

        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun processLoop() {
        val inputBuffer = FloatArray(bufferSize)
        val outputBuffer = FloatArray(bufferSize)

        while (isRunning) {
            // ✅ BASAHIN ANG TUNOG NG GITARA
            val read = recorder?.read(inputBuffer, 0, inputBuffer.size, AudioRecord.READ_BLOCKING) ?: -1
            if (read <= 0) {
                Thread.sleep(5)
                continue
            }

            // ✅ IPROSESO → IPASA SA MIXER → PALABAS!
            for (i in 0 until read) {
                outputBuffer[i] = AudioMixer.process(inputBuffer[i])
            }

            // ✅ IPALABAS SA SPEAKER!
            track?.write(outputBuffer, 0, read)
        }
    }

    fun stop() {
        isRunning = false
        recorder?.stop()
        recorder?.release()
        recorder = null
        track?.stop()
        track?.release()
        track = null
    }
}
