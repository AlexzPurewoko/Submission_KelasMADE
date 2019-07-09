package id.apwdevs.app.catalogue.model.onUserMain

import android.os.Parcelable
import id.apwdevs.app.catalogue.model.GenreModel
import id.apwdevs.app.catalogue.model.ResettableItem
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MovieAboutModel(
    val id: Int,
    var releaseDate: CharSequence?,
    val posterPath: String?,
    val isAdult: Boolean,
    val overview: String?,
    val genres: List<GenreModel>?,
    var originalTitle: CharSequence?,
    val originalLanguage: String?,
    var title: CharSequence?,
    val backdropPath: String?,
    val popularity: Double,
    val voteCount: Int,
    val voteAverage: Double,
    val video: Boolean
) : ResettableItem, Parcelable {
    override fun onReset() {
        title = title.toString()
        originalTitle = originalTitle.toString()
        releaseDate = releaseDate.toString()
        genres?.forEach {
            it.onReset()
        }
    }

}