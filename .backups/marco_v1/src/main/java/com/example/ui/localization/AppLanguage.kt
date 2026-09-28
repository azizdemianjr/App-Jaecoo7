package com.example.ui.localization

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val flag: String,
    val shortCode: String,
    val currencySymbol: String
) {
    PT_BR("pt-BR", "Português (Brasil)", "🇧🇷", "PT", "R$"),
    EN_US("en-US", "English (US)", "🇺🇸", "EN", "$");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return when (code?.lowercase()) {
                "en", "en-us", "en_us" -> EN_US
                else -> PT_BR
            }
        }
    }
}
