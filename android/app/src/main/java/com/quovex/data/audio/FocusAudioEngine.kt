package com.quovex.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.quovex.domain.model.NoiseType
import com.quovex.domain.model.SoundscapePreset
import com.quovex.domain.model.SoundscapePresets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.sin

/**
 * High-performance, offline DSP audio synthesizer that generates stereo binaural beats,
 * filtered colored noises (white, pink, brown), and procedural rain ambience directly
 * onto an Android AudioTrack stream.
 */
@Singleton
class FocusAudioEngine @Inject constructor() {

    private val audioScope = CoroutineScope(Dispatchers.Default)
    private var synthJob: Job? = null
    private var audioTrack: AudioTrack? = null

    @Volatile
    private var activePreset: SoundscapePreset = SoundscapePresets.NONE

    @Volatile
    private var targetVolume: Float = 0.75f

    @Volatile
    private var currentVolume: Float = 0.0f

    @Volatile
    private var isPlaying: Boolean = false

    private val sampleRate = 44100
    private val bufferSizeSamples = 2048

    fun startPlayback(preset: SoundscapePreset, volume: Float) {
        activePreset = preset
        targetVolume = volume.coerceIn(0.0f, 1.0f)

        if (preset == SoundscapePresets.NONE) {
            stopPlayback()
            return
        }

        if (isPlaying && synthJob?.isActive == true) {
            return
        }

        isPlaying = true
        initAudioTrack()

        synthJob?.cancel()
        synthJob = audioScope.launch {
            synthesizeAudioLoop()
        }
    }

    fun updatePreset(preset: SoundscapePreset) {
        activePreset = preset
        if (preset == SoundscapePresets.NONE) {
            stopPlayback()
        } else if (!isPlaying) {
            startPlayback(preset, targetVolume)
        }
    }

    fun updateVolume(volume: Float) {
        targetVolume = volume.coerceIn(0.0f, 1.0f)
    }

    fun pausePlayback() {
        isPlaying = false
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
        } catch (_: Exception) {}
    }

    fun stopPlayback() {
        isPlaying = false
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        currentVolume = 0.0f
    }

    private fun initAudioTrack() {
        if (audioTrack != null) return

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = (minBufferSize * 2).coerceAtLeast(bufferSizeSamples * 4)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build()

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(audioAttributes)
            .setAudioFormat(audioFormat)
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        try {
            audioTrack?.play()
        } catch (_: Exception) {}
    }

    private fun synthesizeAudioLoop() {
        val buffer = ShortArray(bufferSizeSamples * 2) // Interleaved Stereo (L, R)
        val random = Random()

        var phaseLeft = 0.0
        var phaseRight = 0.0

        // Pink noise filter state registers (Voss-McCartney 6-pole)
        var b0 = 0.0
        var b1 = 0.0
        var b2 = 0.0
        var b3 = 0.0
        var b4 = 0.0
        var b5 = 0.0
        var b6 = 0.0

        // Brown noise filter register
        var brownAccumulator = 0.0

        // Rain LFO modulator state
        var rainLfoPhase = 0.0

        while (isPlaying) {
            val preset = activePreset
            if (preset == SoundscapePresets.NONE) {
                break
            }

            val baseFreq = preset.baseFrequencyHz
            val beatFreq = preset.beatFrequencyHz
            val leftInc = (2.0 * PI * baseFreq) / sampleRate
            val rightInc = (2.0 * PI * (baseFreq + beatFreq)) / sampleRate
            val rainLfoInc = (2.0 * PI * 0.25) / sampleRate // 0.25 Hz slow gust envelope

            val volumeFadeRate = 0.002f // Smooth transition per sample

            for (i in 0 until bufferSizeSamples) {
                // Smooth volume interpolation
                if (currentVolume < targetVolume) {
                    currentVolume = (currentVolume + volumeFadeRate).coerceAtMost(targetVolume)
                } else if (currentVolume > targetVolume) {
                    currentVolume = (currentVolume - volumeFadeRate).coerceAtLeast(targetVolume)
                }

                var leftSample = 0.0
                var rightSample = 0.0

                if (preset.isBinaural) {
                    // Pure stereo sine wave synthesis with phase differentiation
                    leftSample = sin(phaseLeft) * 0.45
                    rightSample = sin(phaseRight) * 0.45

                    phaseLeft += leftInc
                    if (phaseLeft > 2.0 * PI) phaseLeft -= 2.0 * PI

                    phaseRight += rightInc
                    if (phaseRight > 2.0 * PI) phaseRight -= 2.0 * PI

                    // Add very subtle warm low background pink noise floor for pleasantness
                    val white = (random.nextDouble() * 2.0 - 1.0) * 0.05
                    b0 = 0.99886 * b0 + white * 0.0555179
                    b1 = 0.99332 * b1 + white * 0.0750759
                    b2 = 0.96900 * b2 + white * 0.1538520
                    val pinkFloor = (b0 + b1 + b2) * 0.08
                    leftSample += pinkFloor
                    rightSample += pinkFloor
                } else {
                    when (preset.noiseType) {
                        NoiseType.WHITE -> {
                            val whiteL = (random.nextDouble() * 2.0 - 1.0) * 0.25
                            val whiteR = (random.nextDouble() * 2.0 - 1.0) * 0.25
                            leftSample = whiteL
                            rightSample = whiteR
                        }
                        NoiseType.PINK -> {
                            val whiteL = random.nextDouble() * 2.0 - 1.0
                            val whiteR = random.nextDouble() * 2.0 - 1.0

                            b0 = 0.99886 * b0 + whiteL * 0.0555179
                            b1 = 0.99332 * b1 + whiteL * 0.0750759
                            b2 = 0.96900 * b2 + whiteL * 0.1538520
                            b3 = 0.86650 * b3 + whiteL * 0.3104856
                            b4 = 0.55000 * b4 + whiteL * 0.5329522
                            b5 = -0.7616 * b5 - whiteL * 0.0168980
                            leftSample = (b0 + b1 + b2 + b3 + b4 + b5 + b6 + whiteL * 0.5362) * 0.04
                            b6 = whiteL * 0.115926
                            rightSample = leftSample * 0.95 + whiteR * 0.01
                        }
                        NoiseType.BROWN -> {
                            val whiteL = random.nextDouble() * 2.0 - 1.0
                            brownAccumulator = (brownAccumulator + (0.02 * whiteL)) / 1.02
                            leftSample = brownAccumulator * 3.5 * 0.15
                            rightSample = leftSample
                        }
                        NoiseType.RAIN -> {
                            // Pink noise base + slow sinusoid envelope modulation + random drops
                            val whiteL = random.nextDouble() * 2.0 - 1.0
                            b0 = 0.99886 * b0 + whiteL * 0.0555179
                            b1 = 0.99332 * b1 + whiteL * 0.0750759
                            b2 = 0.96900 * b2 + whiteL * 0.1538520
                            val pinkBase = (b0 + b1 + b2) * 0.08

                            // Rain swell modulation
                            val swell = 0.65 + 0.35 * sin(rainLfoPhase)
                            rainLfoPhase += rainLfoInc
                            if (rainLfoPhase > 2.0 * PI) rainLfoPhase -= 2.0 * PI

                            // Random droplet spikes
                            val drop = if (random.nextDouble() < 0.001) (random.nextDouble() * 0.2) else 0.0

                            leftSample = (pinkBase * swell + drop) * 0.4
                            rightSample = (pinkBase * (0.65 + 0.35 * sin(rainLfoPhase + 1.0)) + drop) * 0.4
                        }
                        NoiseType.NONE -> {
                            leftSample = 0.0
                            rightSample = 0.0
                        }
                    }
                }

                // Scale to 16-bit PCM and apply current master volume
                val finalL = (leftSample * currentVolume * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                val finalR = (rightSample * currentVolume * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())

                buffer[i * 2] = finalL.toShort()
                buffer[i * 2 + 1] = finalR.toShort()
            }

            try {
                audioTrack?.write(buffer, 0, buffer.size)
            } catch (_: Exception) {
                break
            }
        }
    }
}
