package id.apwdevs.app.countryflags

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import java.util.*

object GetCountryFlags {
    fun getFlagsDrawable(context: Context, iso3166Code: String): Drawable? {
        val res = context.resources
        val resId = res.getIdentifier(
            iso3166Code,
            "drawable",
            "id.apwdevs.app.countryflags"
        )
        return if (resId == 0) null else ContextCompat.getDrawable(context, resId)
    }

    fun getFlagsDrawable(context: Context, locale: Locale): Drawable? {
        return GetCountryFlags.getFlagsDrawable(context, locale.country.toLowerCase())
    }


}