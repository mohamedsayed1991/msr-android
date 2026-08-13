package com.example.config

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object AppConfig {
    const val BASE_URL = "http://13.53.130.231:8080/"
    
    // Dynamically updated during auto-discovery, otherwise loaded from preferences
    var tenantUsername: String = ""
    var tenantSystemName: String = "شبكة MSR"
    var walletPhone: String = ""
    var currency: String = "ج.م"

    /**
     * Formatting helper function to strip redundant decimals.
     * e.g., 10.0 -> "10", 500.00 -> "500", 10.50 -> "10.5", 10.75 -> "10.75"
     */
    fun formatPrice(price: Number?): String {
        if (price == null) return "0"
        val d = price.toDouble()
        if (d % 1.0 == 0.0) {
            return d.toLong().toString()
        }
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#.##", symbols)
        return df.format(d)
    }

    /**
     * Formats price with currency appended, e.g. "10 ج.م" or "10.5 USD"
     */
    fun formatPriceWithCurrency(price: Number?): String {
        return "${formatPrice(price)} $currency"
    }
}

