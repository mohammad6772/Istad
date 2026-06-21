package com.example.util

import android.content.Context
import android.os.Build
import java.util.Locale

object Localization {
    var currentLanguage = "fa" // "fa" or "en"

    fun t(fa: String, en: String): String {
        return if (currentLanguage == "fa") fa else en
    }

    fun isFarsi(): Boolean = currentLanguage == "fa"
}

object LocaleHelper {
    
    fun setLocale(context: Context, language: String): Context {
        persist(context, language)
        Localization.currentLanguage = language
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, language)
        } else {
            updateResourcesLegacy(context, language)
        }
    }

    private fun persist(context: Context, language: String) {
        val preferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        preferences.edit().putString("app_lang", language).apply()
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    private fun updateResourcesLegacy(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = resources.configuration
        configuration.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale)
        }
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }
}
