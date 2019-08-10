package id.apwdevs.app.favoritedisplayer.model

import android.os.Parcelable
import android.text.SpannableString
import androidx.core.text.clearSpans
import id.apwdevs.app.favoritedisplayer.plugin.ResettableItem
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FavoriteEntity(
    val id: Int,
    val title: String,
    val contentType: Int,
    val releaseDate: String,
    val overview: String?,
    val genreIds: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val voteCount: Int,
    val voteAverage: Double
) : Parcelable, ResettableItem {

    @IgnoredOnParcel
    var titleSpan: SpannableString? = null

    @IgnoredOnParcel
    var releaseDateSpan: SpannableString? = null

    override fun onReset() {
        if (titleSpan == null)
            titleSpan = SpannableString(title)
        if (releaseDateSpan == null)
            releaseDateSpan = SpannableString(releaseDate)

        titleSpan?.clearSpans()
        releaseDateSpan?.clearSpans()
    }

}