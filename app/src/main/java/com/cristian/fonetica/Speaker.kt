package com.cristian.fonetica

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Reproduce sonidos:
 *  - Grabaciones incluidas en res/raw (voz neural): "ph_<id>" = fonema aislado, "w_<palabra>" = palabra.
 *  - Si no hay grabación, usa el motor de voz del teléfono (Text-to-Speech).
 */
class Speaker(private val context: Context, onStatus: (Status) -> Unit) {

    enum class Status { LOADING, READY, MISSING_VOICE, NO_ENGINE }

    private var player: MediaPlayer? = null
    private var ready = false
    private var sequence = 0

    private val attrs = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        if (status != TextToSpeech.SUCCESS) {
            onStatus(Status.NO_ENGINE)
        } else {
            val result = engine().setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                onStatus(Status.MISSING_VOICE)
            } else {
                ready = true
                onStatus(Status.READY)
            }
        }
    }

    private fun engine() = tts

    /** Lento: la voz del teléfono va a 0.5 y las grabaciones al 70 %. */
    var slow: Boolean = false
        set(value) {
            field = value
            tts.setSpeechRate(if (value) 0.5f else 0.85f)
        }

    init {
        onStatus(Status.LOADING)
        tts.setSpeechRate(0.85f)
    }

    @SuppressLint("DiscouragedApi")
    private fun rawId(name: String): Int =
        context.resources.getIdentifier(name, "raw", context.packageName)

    private fun wordRes(word: String): Int {
        val key = word.lowercase(Locale.US).filter { it in 'a'..'z' || it in '0'..'9' }
        return if (key.isEmpty()) 0 else rawId("w_$key")
    }

    /** Palabra o frase: grabaciones si existen todas, si no la voz del teléfono. */
    fun speak(text: String) {
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val ids = words.map { wordRes(it) }
        if (ids.isNotEmpty() && ids.all { it != 0 }) playSequence(ids) else ttsSpeak(text)
    }

    /** Fonema: sonido aislado (vocales y diptongos) y después la palabra de ejemplo. */
    fun playPhoneme(p: Phoneme) {
        val example = p.examples.first()
        val wordId = wordRes(example)
        if (wordId == 0) {
            ttsSpeak(example)
            return
        }
        playSequence(listOf(rawId("ph_${p.id}"), wordId).filter { it != 0 })
    }

    private fun ttsSpeak(text: String) {
        stopAll()
        if (ready) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "u${System.nanoTime()}")
    }

    private fun playSequence(ids: List<Int>) {
        stopAll()
        val token = sequence
        playAt(ids, 0, token)
    }

    private fun playAt(ids: List<Int>, i: Int, token: Int) {
        if (i >= ids.size || token != sequence) return
        val mp = MediaPlayer.create(context, ids[i], attrs, 0)
        if (mp == null) {
            playAt(ids, i + 1, token)
            return
        }
        player = mp
        mp.setOnCompletionListener {
            it.release()
            if (player === it) player = null
            playAt(ids, i + 1, token)
        }
        mp.start()
        if (slow) runCatching { mp.playbackParams = mp.playbackParams.setSpeed(0.7f) }
    }

    private fun stopAll() {
        sequence++
        tts.stop()
        player?.let { runCatching { it.stop() }; it.release() }
        player = null
    }

    fun shutdown() {
        stopAll()
        tts.shutdown()
    }
}
