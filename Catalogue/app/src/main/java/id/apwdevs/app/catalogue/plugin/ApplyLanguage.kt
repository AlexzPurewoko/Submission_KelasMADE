package id.apwdevs.app.catalogue.plugin

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import java.util.*

object ApplyLanguage {
    fun wrap(context: Context, locale: Locale): ContextWrapper {
        val res = context.resources
        val configuration = res.configuration
        val newContext: Context
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            newContext = context.createConfigurationContext(configuration.apply {
                val localeList = LocaleList(locale)
                configuration.locales = localeList
                LocaleList.setDefault(localeList)
            })
        } else {
            newContext = context.createConfigurationContext(configuration.apply {
                setLocale(locale)
            })
        }
        return ContextWrapper(newContext)
    }
}