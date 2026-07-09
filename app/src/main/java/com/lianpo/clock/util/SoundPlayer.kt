package com.lianpo.clock.util

import android.media.AudioManager
import android.media.ToneGenerator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundPlayer @Inject constructor() {

    private var toneGenerator: ToneGenerator? = null

    fun playSound(soundType: String = "default") {
        stopSound()

        toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)

        val toneType = when (soundType) {
            "crisp" -> ToneGenerator.TONE_PROP_ACK
            "gentle" -> ToneGenerator.TONE_PROP_BEEP2
            else -> ToneGenerator.TONE_PROP_BEEP
        }

        toneGenerator?.startTone(toneType, 2000)
    }

    fun stopSound() {
        toneGenerator?.stopTone()
        toneGenerator?.release()
        toneGenerator = null
    }
}