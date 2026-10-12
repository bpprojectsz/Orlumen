package com.zdmgold.orlumen.designs.aod

private const val TEEN_LIMIT = 20
private const val TENS_DIVISOR = 10
private const val MAX_VALUE = 59

private val units = listOf(
    "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
    "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"
)

private val tens = listOf("", "", "twenty", "thirty", "forty", "fifty")

/** English words for the numbers a clock needs, 0 to 59. */
internal object NumberWords {
    fun english(value: Int): String {
        require(value in 0..MAX_VALUE) { "value out of range: $value" }
        if (value < TEEN_LIMIT) return units[value]
        val remainder = value % TENS_DIVISOR
        val tensWord = tens[value / TENS_DIVISOR]
        return if (remainder == 0) tensWord else "$tensWord-${units[remainder]}"
    }

    fun englishTitleCase(value: Int): String = english(value).replaceFirstChar { it.uppercase() }
}
