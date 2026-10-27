package com.cristian.fonetica

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private lateinit var speaker: Speaker
    private val voiceStatus = mutableStateOf(Speaker.Status.LOADING)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        speaker = Speaker(this) { voiceStatus.value = it }
        setContent {
            AppTheme {
                PronunciationScreen(
                    speaker = speaker,
                    voiceStatus = voiceStatus.value,
                    onInstallVoice = {
                        runCatching {
                            startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
                        }
                    },
                )
            }
        }
    }

    override fun onDestroy() {
        speaker.shutdown()
        super.onDestroy()
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkColorScheme(primary = Color(0xFF8CC8F0), secondary = Color(0xFFFFD166))
        else -> lightColorScheme(primary = Color(0xFF1F5F8B), secondary = Color(0xFFB8860B))
    }
    MaterialTheme(colorScheme = colors, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PronunciationScreen(
    speaker: Speaker,
    voiceStatus: Speaker.Status,
    onInstallVoice: () -> Unit,
) {
    val context = LocalContext.current
    var dictLoaded by remember { mutableStateOf(PhoneticDictionary.isLoaded) }
    LaunchedEffect(Unit) {
        PhoneticDictionary.load(context)
        dictLoaded = true
    }
    var slow by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(slow) { speaker.slow = slow }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Fonética Inglesa") }) },
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "British English (RP) · IPA transcription with audio",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SearchCard(
                dictLoaded = dictLoaded,
                slow = slow,
                onSlowChange = { slow = it },
                onSpeak = { speaker.speak(it) },
            )
            if (voiceStatus == Speaker.Status.MISSING_VOICE || voiceStatus == Speaker.Status.NO_ENGINE) {
                VoiceWarning(voiceStatus, onInstallVoice)
            }
            HowToUse()
        }
    }
}

@Composable
fun SearchCard(
    dictLoaded: Boolean,
    slow: Boolean,
    onSlowChange: (Boolean) -> Unit,
    onSpeak: (String) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    var lastQuery by rememberSaveable { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current

    val results = remember(lastQuery, dictLoaded) {
        if (dictLoaded && lastQuery.isNotBlank()) PhoneticDictionary.transcribe(lastQuery) else emptyList()
    }

    fun submit() {
        val text = input.trim()
        if (text.isEmpty()) return
        lastQuery = text
        keyboard?.hide()
        onSpeak(text)
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Word or phrase", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.size(8.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g. pronunciation") },
                trailingIcon = {
                    if (input.isNotEmpty()) {
                        IconButton(onClick = { input = ""; lastQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )
            Spacer(Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { submit() }, enabled = input.isNotBlank()) {
                    Text("Show pronunciation")
                }
                Spacer(Modifier.width(12.dp))
                FilterChip(
                    selected = slow,
                    onClick = { onSlowChange(!slow) },
                    label = { Text("Slow") },
                )
            }

            if (lastQuery.isNotBlank()) {
                Spacer(Modifier.size(12.dp))
                if (!dictLoaded) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Loading dictionary…")
                    }
                } else {
                    val ipa = results.joinToString(" ") { it.ipa ?: "${it.word}?" }
                    val missing = results.filter { it.ipa == null }.map { it.word }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "/$ipa/",
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                        FilledTonalIconButton(onClick = { onSpeak(lastQuery) }) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Listen again")
                        }
                    }
                    if (missing.isNotEmpty()) {
                        Text(
                            "Not in the dictionary: ${missing.joinToString(", ")}. You can still listen to it.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HowToUse() {
    val steps = listOf(
        "Type an English word or phrase in the box.",
        "Tap “Show pronunciation” (or the Done key). You will see the phonetic transcription in IPA and hear it read aloud.",
        "Tap ▶ to listen again. Turn on “Slow” to hear it at a slower speed.",
        "Stress marks: ˈ comes before the syllable with main stress, ˌ before secondary stress. A long vowel is shown with ː.",
        "Single words are shown in their full (dictionary) form. In fast speech, short words like to, can or are are often reduced to /tə/, /kən/, /ə/.",
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "HOW TO USE",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.5.sp,
        )
        steps.forEachIndexed { i, step ->
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                        append("${i + 1}.  ")
                    }
                    append(step)
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            "No sound? Check that your phone is not on silent and turn up the media volume.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun VoiceWarning(status: Speaker.Status, onInstallVoice: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                if (status == Speaker.Status.NO_ENGINE)
                    "No text-to-speech engine is installed. Install “Speech Services by Google” from the Play Store."
                else
                    "The British English voice is missing on this phone. Common words still play; for other words, install the voice.",
            )
            if (status == Speaker.Status.MISSING_VOICE) {
                TextButton(onClick = onInstallVoice) { Text("Install voice") }
            }
        }
    }
}
