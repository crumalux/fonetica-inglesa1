package com.cristian.fonetica

/**
 * Un fonema del inglés británico (RP), tabla de 44 sonidos.
 * @param symbol símbolo IPA que se muestra en el botón
 * @param id     nombre corto (ASCII) para buscar una grabación opcional en res/raw: "ph_<id>"
 * @param examples palabras de ejemplo; la primera es la que se pronuncia al tocar el botón
 * @param hint   cómo suena, explicado para hispanohablantes
 */
data class Phoneme(
    val symbol: String,
    val id: String,
    val examples: List<String>,
    val hint: String,
)

data class PhonemeGroup(val title: String, val items: List<Phoneme>)

val phonemeGroups = listOf(
    PhonemeGroup(
        "Vocales",
        listOf(
            Phoneme("iː", "iy", listOf("see", "green", "eat"), "i larga y tensa, labios en sonrisa"),
            Phoneme("ɪ", "ih", listOf("sit", "fish", "busy"), "i corta y relajada, entre i y e"),
            Phoneme("e", "e", listOf("bed", "head", "said"), "como la e española"),
            Phoneme("æ", "ae", listOf("cat", "black", "apple"), "a muy abierta, tirando a e"),
            Phoneme("ʌ", "ah", listOf("cup", "love", "money"), "a corta y central, breve"),
            Phoneme("ɑː", "aa", listOf("father", "car", "bath"), "a larga y profunda, al fondo de la boca"),
            Phoneme("ɒ", "o", listOf("hot", "dog", "watch"), "o corta y abierta, labios apenas redondeados"),
            Phoneme("ɔː", "ao", listOf("saw", "thought", "door"), "o larga, labios redondeados"),
            Phoneme("ʊ", "uh", listOf("book", "put", "could"), "u corta y relajada"),
            Phoneme("uː", "uw", listOf("food", "blue", "two"), "u larga"),
            Phoneme("ɜː", "er", listOf("bird", "work", "learn"), "vocal larga central, sin r: ni e ni o"),
            Phoneme("ə", "schwa", listOf("about", "sofa", "teacher"), "schwa: vocal neutra y débil, siempre sin acento"),
        ),
    ),
    PhonemeGroup(
        "Diptongos",
        listOf(
            Phoneme("ɪə", "ia", listOf("near", "here", "ear"), "de i hacia schwa, sin r final"),
            Phoneme("eə", "ea", listOf("hair", "where", "care"), "e larga que se apaga hacia schwa, sin r"),
            Phoneme("ʊə", "ua", listOf("pure", "tour", "cure"), "de u hacia schwa (muchos hablantes dicen ɔː)"),
            Phoneme("eɪ", "ey", listOf("day", "rain", "make"), "como «ei» en «rey»"),
            Phoneme("aɪ", "ay", listOf("my", "time", "fly"), "como «ai» en «aire»"),
            Phoneme("ɔɪ", "oy", listOf("boy", "coin", "toy"), "como «oi» en «hoy»"),
            Phoneme("əʊ", "ow", listOf("go", "home", "boat"), "de schwa hacia u: ni «ou» ni «o»"),
            Phoneme("aʊ", "aw", listOf("now", "house", "cow"), "como «au» en «auto»"),
        ),
    ),
    PhonemeGroup(
        "Consonantes",
        listOf(
            Phoneme("p", "p", listOf("pen", "happy", "stop"), "p con soplo de aire"),
            Phoneme("b", "b", listOf("bad", "rabbit", "job"), "b bien sonora, labios cerrados"),
            Phoneme("t", "t", listOf("tea", "better", "cat"), "t con la lengua en las encías, con aire"),
            Phoneme("d", "d", listOf("did", "lady", "bed"), "d con la lengua en las encías"),
            Phoneme("k", "k", listOf("cat", "school", "back"), "k con soplo de aire"),
            Phoneme("ɡ", "g", listOf("get", "bigger", "dog"), "g de «gato»"),
            Phoneme("f", "f", listOf("fish", "coffee", "laugh"), "como la f española"),
            Phoneme("v", "v", listOf("van", "never", "love"), "labio inferior y dientes, con vibración"),
            Phoneme("θ", "th", listOf("think", "both", "three"), "como la z de España"),
            Phoneme("ð", "dh", listOf("this", "mother", "the"), "como la d suave de «nada»"),
            Phoneme("s", "s", listOf("see", "city", "bus"), "s sorda"),
            Phoneme("z", "z", listOf("zoo", "easy", "is"), "s sonora, zumbido de abeja"),
            Phoneme("ʃ", "sh", listOf("she", "nation", "fish"), "«sh» para pedir silencio"),
            Phoneme("ʒ", "zh", listOf("vision", "measure", "usual"), "como la «ll» rioplatense"),
            Phoneme("h", "h", listOf("hat", "who", "ahead"), "j muy suave, sólo aire"),
            Phoneme("tʃ", "ch", listOf("chair", "teacher", "watch"), "como la ch española"),
            Phoneme("dʒ", "jh", listOf("jump", "age", "bridge"), "como «ch» pero sonora"),
            Phoneme("m", "m", listOf("man", "summer", "time"), "como la m española"),
            Phoneme("n", "n", listOf("no", "dinner", "sun"), "como la n española"),
            Phoneme("ŋ", "ng", listOf("sing", "thinking", "long"), "n nasal de «tengo», sin la g"),
            Phoneme("l", "l", listOf("leg", "hello", "feel"), "l clara antes de vocal, oscura al final"),
            Phoneme("r", "r", listOf("red", "sorry", "very"), "r suave sin vibrar; sólo suena antes de vocal (en «car» no)"),
            Phoneme("w", "w", listOf("wet", "window", "away"), "como «u» en «huevo»"),
            Phoneme("j", "y", listOf("yes", "you", "music"), "como la «i» de «hielo»"),
        ),
    ),
)
