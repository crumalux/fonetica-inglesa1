package com.cristian.fonetica

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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
                FoneticaScreen(
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FoneticaScreen(
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

    var selectedSymbol by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = remember(selectedSymbol) {
        phonemeGroups.flatMap { it.items }.firstOrNull { it.symbol == selectedSymbol }
    }
    var lastShown by remember { mutableStateOf<Phoneme?>(null) }
    if (selected != null) lastShown = selected
    var slow by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(slow) { speaker.slow = slow }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Fonética Inglesa") })
        },
        bottomBar = {
            AnimatedVisibility(
                visible = selected != null,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
            ) {
                lastShown?.let { p ->
                    PhonemeDetail(
                        phoneme = p,
                        onSpeakWord = { speaker.speak(it) },
                        onPlay = { speaker.playPhoneme(p) },
                        onClose = { selectedSymbol = null },
                    )
                }
            }
        },
    ) { inner ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 72.dp),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = inner.calculateTopPadding() + 8.dp,
                bottom = inner.calculateBottomPadding() + 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SearchCard(
                    dictLoaded = dictLoaded,
                    slow = slow,
                    onSlowChange = { slow = it },
                    onSpeak = { speaker.speak(it) },
                )
            }
            if (voiceStatus == Speaker.Status.MISSING_VOICE || voiceStatus == Speaker.Status.NO_ENGINE) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    VoiceWarning(voiceStatus, onInstallVoice)
                }
            }
            phonemeGroups.forEach { group ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        group.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp),
                    )
                }
                items(group.items, key = { it.id }) { p ->
                    PhonemeButton(
                        phoneme = p,
                        isSelected = p.symbol == selectedSymbol,
                        onClick = {
                            selectedSymbol = p.symbol
                            speaker.playPhoneme(p)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun PhonemeButton(phoneme: Phoneme, isSelected: Boolean, onClick: () -> Unit) {
    val colors = if (isSelected) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
    Card(
        onClick = onClick,
        colors = colors,
        modifier = Modifier.aspectRatio(1f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(phoneme.symbol, fontSize = 26.sp, fontWeight = FontWeight.Medium)
            Text(
                phoneme.examples.first(),
                fontSize = 11.sp,
                maxLines = 1,
            )
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
            Text("Escribí una palabra o frase en inglés", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.size(8.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("p. ej. pronunciation") },
                trailingIcon = {
                    if (input.isNotEmpty()) {
                        IconButton(onClick = { input = ""; lastQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Borrar")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )
            Spacer(Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { submit() }, enabled = input.isNotBlank()) {
                    Text("Ver fonética")
                }
                Spacer(Modifier.width(12.dp))
                FilterChip(
                    selected = slow,
                    onClick = { onSlowChange(!slow) },
                    label = { Text("Lento") },
                )
            }

            if (lastQuery.isNotBlank()) {
                Spacer(Modifier.size(12.dp))
                if (!dictLoaded) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Cargando diccionario…")
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
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Escuchar")
                        }
                    }
                    if (missing.isNotEmpty()) {
                        Text(
                            "No encontré: ${missing.joinToString(", ")} (igual podés escucharla)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhonemeDetail(
    phoneme: Phoneme,
    onSpeakWord: (String) -> Unit,
    onPlay: () -> Unit,
    onClose: () -> Unit,
) {
    Surface(
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("/${phoneme.symbol}/", fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(12.dp))
                Text(phoneme.hint, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                FilledTonalIconButton(onClick = onPlay) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Repetir")
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                }
            }
            Spacer(Modifier.size(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                phoneme.examples.forEach { w ->
                    val ipa = PhoneticDictionary.lookupWord(w)
                    AssistChip(
                        onClick = { onSpeakWord(w) },
                        label = { Text(if (ipa != null) "$w  /$ipa/" else w) },
                        leadingIcon = { Icon(Icons.Filled.PlayArrow, contentDescription = null, Modifier.size(16.dp)) },
                    )
                }
            }
        }
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
                    "No hay un motor de voz instalado. Instalá «Speech Services by Google» desde Play Store."
                else
                    "Falta la voz en inglés (EE. UU.) en tu teléfono.",
                textAlign = TextAlign.Start,
            )
            if (status == Speaker.Status.MISSING_VOICE) {
                TextButton(onClick = onInstallVoice) { Text("Instalar voz") }
            }
        }
    }
}
