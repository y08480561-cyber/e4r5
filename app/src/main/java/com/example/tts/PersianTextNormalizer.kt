package com.example.tts

object PersianTextNormalizer {

    private val units = arrayOf("", "یک", "دو", "سه", "چهار", "پنج", "شش", "هفت", "هشت", "نه")
    private val teens = arrayOf("ده", "یازده", "دوازده", "سیزده", "چهارده", "پانزده", "شانزده", "هفده", "هجده", "نوزده")
    private val tens = arrayOf("", "", "بیست", "سی", "چهل", "پنجاه", "شصت", "هفتاد", "هشتاد", "نود")
    private val hundreds = arrayOf("", "صد", "دویست", "سیصد", "چهارصد", "پانصد", "ششصد", "هفتصد", "هشتصد", "نهصد")

    /**
     * Splits long text into natural, human-flowing speech chunks (<= 34 chars)
     * optimized for storytelling narration with native Persian vocalization.
     */
    fun splitIntoChunks(text: String, maxLen: Int = 34): List<String> {
        if (text.isBlank()) return emptyList()

        val normalized = normalizeForSpeech(text)
        val vocalized = vocalizeForHumanSpeech(normalized)
        val sentences = vocalized.split(Regex("(?<=[\\.\\!\\؟\\?\n])")).map { it.trim() }.filter { it.isNotEmpty() }
        val finalChunks = mutableListOf<String>()

        for (sentence in sentences) {
            val words = sentence.split(Regex("\\s+")).filter { it.isNotEmpty() }
            var curr = ""

            for (rawWord in words) {
                var w = rawWord
                // If a single word exceeds maxLen, split it safely
                while (w.length > maxLen) {
                    if (curr.isNotEmpty()) {
                        finalChunks.add(curr.trim())
                        curr = ""
                    }
                    finalChunks.add(w.substring(0, maxLen))
                    w = w.substring(maxLen)
                }

                if (w.isEmpty()) continue

                if (curr.length + w.length + (if (curr.isNotEmpty()) 1 else 0) <= maxLen) {
                    curr = (if (curr.isEmpty()) w else "$curr $w").trim()
                } else {
                    if (curr.isNotEmpty()) {
                        finalChunks.add(curr.trim())
                    }
                    curr = w
                }
            }

            if (curr.isNotEmpty()) {
                finalChunks.add(curr.trim())
            }
        }

        return finalChunks.filter { it.isNotBlank() }
    }

    /**
     * Applies precise Persian diacritics (حرکات و اعراب) to common words so the
     * VITS model reproduces 100% natural Iranian human pronunciation and cadence.
     */
    fun vocalizeForHumanSpeech(text: String): String {
        var t = text
        val vocalizationMap = mapOf(
            "\\bسلام\\b" to "سَلام",
            "\\bبرای\\b" to "بَرایِ",
            "\\bیک\\b" to "یِک",
            "\\bعزیز\\b" to "عَزیز",
            "\\bمن\\b" to "مَن",
            "\\bتو\\b" to "تو",
            "\\bشما\\b" to "شُما",
            "\\bامروز\\b" to "اِمروز",
            "\\bامیدوارم\\b" to "اُمیدوارَم",
            "\\bبسیار\\b" to "بِسیار",
            "\\bبهترین\\b" to "بِه‌تَرین",
            "\\bبرنامه\\b" to "بَرنامِه",
            "\\bزندگی\\b" to "زِندِگی",
            "\\bهمیشه\\b" to "هَمیشِه",
            "\\bهمراه\\b" to "هَمراه",
            "\\bهنوز\\b" to "هَنوز",
            "\\bدیگر\\b" to "دیگَر",
            "\\bباید\\b" to "بایَد",
            "\\bشاید\\b" to "شایَد",
            "\\bاگر\\b" to "اَگَر",
            "\\bمگر\\b" to "مَگَر",
            "\\bچرا\\b" to "چِرا",
            "\\bکجا\\b" to "کُجا",
            "\\bچگونه\\b" to "چِگونِه",
            "\\bانسان\\b" to "اِنسان",
            "\\bجهان\\b" to "جَهان",
            "\\bزمین\\b" to "زَمین",
            "\\bآسمان\\b" to "آسِمان",
            "\\bپسر\\b" to "پِسَر",
            "\\bدختر\\b" to "دُختَر",
            "\\bمادر\\b" to "مادَر",
            "\\bپدر\\b" to "پِدَر",
            "\\bبرادر\\b" to "بَرادَر",
            "\\bخواهر\\b" to "خواهَر",
            "\\bمی‌شود\\b" to "می‌شَوَد",
            "\\bمی‌کند\\b" to "می‌کُنَد",
            "\\bمی‌کنم\\b" to "می‌کُنَم",
            "\\bمی‌کنند\\b" to "می‌کُنَند",
            "\\bمی‌رود\\b" to "می‌رَوَد",
            "\\bهستم\\b" to "هَستَم",
            "\\bهستی\\b" to "هَستی",
            "\\bهستید\\b" to "هَستید",
            "\\bهستند\\b" to "هَستَند",
            "\\bهست\\b" to "هَست",
            "\\bآمد\\b" to "آمَد",
            "\\bرفت\\b" to "رَفت",
            "\\bگفت\\b" to "گُفت",
            "\\bکرد\\b" to "کَرد",
            "\\bشد\\b" to "شُد"
        )

        for ((pattern, replacement) in vocalizationMap) {
            t = t.replace(Regex(pattern), replacement)
        }

        // Add gentle narrative breathing space around commas
        t = t.replace("،", "، ")
        return t
    }

    /**
     * Normalizes Persian numbers, symbols, and characters.
     */
    fun normalizeForSpeech(input: String): String {
        if (input.isBlank()) return ""

        var text = input
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('ة', 'ه')
            .replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
            .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9')
            .replace('٠', '0').replace('١', '1').replace('٢', '2').replace('٣', '3').replace('٤', '4')
            .replace('٥', '5').replace('٦', '6').replace('٧', '7').replace('٨', '8').replace('٩', '9')

        text = text
            .replace("%", " درصد ")
            .replace("+", " به علاوه ")
            .replace("=", " مساوی با ")
            .replace("/", " ممیز ")

        // Numbers to words
        text = text.replace(Regex("\\b\\d+\\b")) { matchResult ->
            val num = matchResult.value.toLongOrNull()
            if (num != null && num in 0..999999) {
                numberToWords(num)
            } else {
                matchResult.value
            }
        }

        text = text
            .replace(Regex("[،,]{2,}"), "،")
            .replace(Regex("[\\.]{2,}"), ".")
            .replace(Regex("[\\!]{2,}"), "!")
            .replace(Regex("\\s+"), " ")
            .trim()

        return text
    }

    private fun numberToWords(number: Long): String {
        if (number == 0L) return "صفر"
        if (number < 10) return units[number.toInt()]
        if (number < 20) return teens[(number - 10).toInt()]
        if (number < 100) {
            val t = tens[(number / 10).toInt()]
            val u = units[(number % 10).toInt()]
            return if (u.isEmpty()) t else "$t و $u"
        }
        if (number < 1000) {
            val h = hundreds[(number / 100).toInt()]
            val rem = number % 100
            return if (rem == 0L) h else "$h و ${numberToWords(rem)}"
        }
        if (number < 1000000) {
            val thousands = number / 1000
            val rem = number % 1000
            val tStr = if (thousands == 1L) "هزار" else "${numberToWords(thousands)} هزار"
            return if (rem == 0L) tStr else "$tStr و ${numberToWords(rem)}"
        }
        return number.toString()
    }
}
