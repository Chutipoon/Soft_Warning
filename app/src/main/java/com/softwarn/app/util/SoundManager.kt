package com.softwarn.app.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.DataOutputStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.sin

@Singleton
class SoundManager @Inject constructor(@ApplicationContext private val context: Context) {

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<Int, Int>() // resId -> soundPoolId

    // Sound IDs (use these constants throughout the app)
    companion object {
        const val SOUND_NONE = 0
        const val SOUND_CHIME = 1
        const val SOUND_BELL = 2
        const val SOUND_POP = 3
        const val SOUND_BREATH = 4
    }

    fun initialize() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()
        // Load sounds — create programmatically since we don't have audio files yet
        // Use AudioTrack to generate simple tones and save to cache dir, then load
        generateAndLoadTone(SOUND_CHIME, 880f, 400)   // A5, 400ms
        generateAndLoadTone(SOUND_BELL, 528f, 600)    // C5, 600ms
        generateAndLoadTone(SOUND_POP, 1200f, 150)    // short pop
        generateAndLoadTone(SOUND_BREATH, 220f, 800)  // low soft tone
    }

    private fun generateAndLoadTone(soundId: Int, frequency: Float, durationMs: Int) {
        val file = File(context.cacheDir, "tone_$soundId.wav")
        if (!file.exists()) {
            // Generate sine wave PCM → write to File in cacheDir → load into SoundPool
            val sampleRate = 44100
            val numSamples = (sampleRate * durationMs / 1000.0).toInt()
            val buffer = ShortArray(numSamples)
            for (i in buffer.indices) {
                val t = i.toDouble() / sampleRate
                val envelope = when {
                    i < numSamples * 0.1 -> i / (numSamples * 0.1) // attack
                    i > numSamples * 0.7 -> (numSamples - i) / (numSamples * 0.3) // release
                    else -> 1.0
                }
                buffer[i] = (Short.MAX_VALUE * 0.3 * envelope * sin(2 * PI * frequency * t)).toInt().toShort()
            }
            // Write WAV file to cache
            writeWav(file, buffer, sampleRate)
        }
        val id = soundPool?.load(file.absolutePath, 1) ?: return
        soundMap[soundId] = id
    }

    fun play(soundId: Int) {
        if (soundId == SOUND_NONE) return
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT) return
        val poolId = soundMap[soundId] ?: return
        val volume = if (audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE) 0f
                     else audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION).toFloat() /
                          audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
        soundPool?.play(poolId, volume, volume, 1, 0, 1f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        soundMap.clear()
    }

    private fun writeWav(file: File, pcm: ShortArray, sampleRate: Int) {
        // Write standard 44-byte WAV header + PCM data
        // Channels: 1 (mono), BitsPerSample: 16
        val dataSize = pcm.size * 2
        DataOutputStream(file.outputStream()).use { out ->
            out.writeBytes("RIFF")
            out.writeIntLE(36 + dataSize)
            out.writeBytes("WAVEfmt ")
            out.writeIntLE(16)
            out.writeShortLE(1)    // PCM
            out.writeShortLE(1)    // mono
            out.writeIntLE(sampleRate)
            out.writeIntLE(sampleRate * 2)
            out.writeShortLE(2)
            out.writeShortLE(16)
            out.writeBytes("data")
            out.writeIntLE(dataSize)
            for (sample in pcm) out.writeShortLE(sample.toInt())
        }
    }
}

private fun DataOutputStream.writeIntLE(value: Int) {
    writeByte(value and 0xFF)
    writeByte((value shr 8) and 0xFF)
    writeByte((value shr 16) and 0xFF)
    writeByte((value shr 24) and 0xFF)
}

private fun DataOutputStream.writeShortLE(value: Int) {
    writeByte(value and 0xFF)
    writeByte((value shr 8) and 0xFF)
}
