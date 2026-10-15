# Fonética Inglesa (Android)

App en Kotlin + Jetpack Compose (Material 3), compilada para Android 16 (API 36), compatible desde Android 8.

## Qué hace
- **Tabla de fonemas**: los 44 sonidos del inglés británico (RP): 12 vocales, 8 diptongos y 24 consonantes. Al tocar uno se escucha y aparece una ficha con la explicación para hispanohablantes y tres palabras de ejemplo con su transcripción (cada una se puede escuchar).
- **Caja de búsqueda**: escribís una palabra o frase, te devuelve la transcripción IPA (p. ej. `pronunciation → /prəˌnʌnsɪˈeɪʃən/`) y la pronuncia. Botón «Lento» para escuchar a media velocidad.
- **Funciona sin internet**: incluye un diccionario británico de ~138.000 palabras (Britfone e ipa-dict, completados con eSpeak) y audios grabados con una voz neural británica.

## Cómo compilarla e instalarla
1. Instalá **Android Studio** (versión reciente).
2. Descomprimí el .zip y abrí la carpeta con *File → Open*.
3. Esperá a que termine la sincronización de Gradle (descarga lo necesario la primera vez). Si Android Studio sugiere actualizar versiones de AGP o librerías, aceptá.
4. Conectá el teléfono con la *Depuración USB* activada (o usá un emulador) y presioná ▶ *Run*.
   Para generar un APK: *Build → Build App Bundle(s)/APK(s) → Build APK(s)*.

## Sobre el sonido de cada fonema
Android no permite sintetizar un fonema aislado a partir de su símbolo, así que por defecto al tocar un fonema se pronuncia su **palabra de ejemplo** (p. ej. /æ/ → «cat»).
Si querés el sonido puro, podés agregar grabaciones propias: copiá archivos `.mp3`/`.ogg`/`.wav` en `app/src/main/res/raw/` con el nombre `ph_<id>`; la app los usa automáticamente en lugar de la voz.

| Fonema | archivo | Fonema | archivo | Fonema | archivo |
|---|---|---|---|---|---|
| iː | ph_iy | ʌ | ph_ah | p b t d k ɡ | ph_p ph_b ph_t ph_d ph_k ph_g |
| ɪ | ph_ih | ə | ph_schwa | f v | ph_f ph_v |
| ɛ | ph_eh | ɝː | ph_er | θ ð | ph_th ph_dh |
| æ | ph_ae | ɚ | ph_er_weak | s z ʃ ʒ h | ph_s ph_z ph_sh ph_zh ph_h |
| ɑː | ph_aa | eɪ aɪ ɔɪ | ph_ey ph_ay ph_oy | tʃ dʒ | ph_ch ph_jh |
| ɔː | ph_ao | aʊ oʊ | ph_aw ph_ow | m n ŋ | ph_m ph_n ph_ng |
| ʊ | ph_uh | | | l r w j | ph_l ph_r ph_w ph_y |
| uː | ph_uw | | | | |

## Si no suena
Revisá *Ajustes → Sistema → Idiomas → Salida de texto a voz*: motor «Servicios de voz de Google» y voz **English (United Kingdom)** descargada. La app avisa y ofrece instalarla si falta.

## Créditos
Diccionario: Britfone (J. Llarena, MIT) e ipa-dict en_UK (MIT), completados con eSpeak NG. Audios: Kokoro-82M (Apache 2.0), voz bf_emma.
