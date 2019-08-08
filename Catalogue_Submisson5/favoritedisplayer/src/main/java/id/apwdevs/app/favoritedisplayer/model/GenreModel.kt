package id.apwdevs.app.favoritedisplayer.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GenreModel(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    var genreName: String?

) : Parcelable

data class GenreModelResponse(

    @SerializedName("genres")
    val allGenre: List<GenreModel>
) : ClassResponse
