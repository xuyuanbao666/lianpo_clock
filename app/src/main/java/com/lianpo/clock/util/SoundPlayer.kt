package com.lianpo.clock.util

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var toneGenerator: ToneGenerator? = null
    private var mediaPlayer: MediaPlayer? = null

    private val customSoundsDir: File
        get() = File(context.filesDir, "custom_sounds").apply { mkdirs() }

    fun playSound(soundType: String = "default") {
        stopSound()

        // 检查是否是自定义铃声
        if (soundType.startsWith("custom_")) {
            val fileName = soundType.removePrefix("custom_")
            val file = File(customSoundsDir, fileName)
            if (file.exists()) {
                playFromFile(file)
                return
            }
        }

        toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        val toneType = when (soundType) {
            "crisp" -> ToneGenerator.TONE_PROP_ACK
            "gentle" -> ToneGenerator.TONE_PROP_BEEP2
            else -> ToneGenerator.TONE_PROP_BEEP
        }
        toneGenerator?.startTone(toneType, 2000)
    }

    private fun playFromFile(file: File) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.fromFile(file))
                setAudioStreamType(AudioManager.STREAM_NOTIFICATION)
                prepare()
                start()
                setOnCompletionListener {
                    stopSound()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 回退到默认声音
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 2000)
        }
    }

    fun importSound(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = "sound_${System.currentTimeMillis()}.mp3"
            val file = File(customSoundsDir, fileName)

            inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            fileName
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getCustomSounds(): List<Pair<String, String>> {
        return customSoundsDir.listFiles()?.map { file ->
            "custom_${file.name}" to file.nameWithoutExtension
        } ?: emptyList()
    }

    fun deleteCustomSound(fileName: String) {
        val file = File(customSoundsDir, fileName)
        if (file.exists()) {
            file.delete()
        }
    }

    fun stopSound() {
        toneGenerator?.stopTone()
        toneGenerator?.release()
        toneGenerator = null

        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }
}