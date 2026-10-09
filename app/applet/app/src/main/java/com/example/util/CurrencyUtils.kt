package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtils {

    private val indonesianSymbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
        groupingSeparator = '.'
        monetaryDecimalSeparator = ','
    }

    private val formatter = DecimalFormat("#,###", indonesianSymbols)

    fun formatRupiah(amount: Long, includePrefix: Boolean = true): String {
        val formatted = formatter.format(amount)
        return if (includePrefix) "Rp $formatted" else formatted
    }

    fun parseAmount(input: String): Long? {
        val clean = input.replace("[^0-9]".toRegex(), "")
        return clean.toLongOrNull()
    }
}
