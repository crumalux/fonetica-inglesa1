package com.cristian.fonetica

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.zip.GZIPInputStream

/**
 * Diccionario fonético offline (~126.000 palabras) derivado del
 * CMU Pronouncing Dictionary y convertido a IPA (inglés americano).
 */
object PhoneticDictionary {

    private val mutex = Mutex()
    @Volatile private var map: Map<String, String>? = null

    val isLoaded: Boolean get() = map != null

    suspend fun load(context: Context) {
        if (map != null) return
        mutex.withLock {
            if (map != null) return
            map = withContext(Dispatchers.IO) {
                val result = HashMap<String, String>(140_000)
                context.assets.open("cmudict_ipa.tsv.gz").use { raw ->
                    GZIPInputStream(raw).bufferedReader(Charsets.UTF_8).forEachLine { line ->
                        val tab = line.indexOf('\t')
                        if (tab > 0) result[line.substring(0, tab)] = line.substring(tab + 1)
                    }
                }
                result
            }
        }
    }

    /** IPA de una sola palabra, o null si no está en el diccionario. */
    fun lookupWord(word: String): String? {
        val key = word.lowercase(Locale.US).trim { !it.isLetterOrDigit() && it != '\'' }
        if (key.isEmpty()) return null
        return map?.get(key)
    }

    data class WordResult(val word: String, val ipa: String?)

    /** Transcribe una frase completa, palabra por palabra. */
    fun transcribe(text: String): List<WordResult> =
        text.split(Regex("[\\s,;:.!?¿¡\"()]+"))
            .filter { it.isNotBlank() }
            .map { WordResult(it, lookupWord(it.replace('’', '\''))) }
}
