package id.apwdevs.moTvCatalogue.model.onUserMain

import android.os.Parcelable
import id.apwdevs.moTvCatalogue.model.GenreModel
import id.apwdevs.moTvCatalogue.model.ResettableItem
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MovieAboutModel(
    val id: Int,
    var releaseDate: String?,
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