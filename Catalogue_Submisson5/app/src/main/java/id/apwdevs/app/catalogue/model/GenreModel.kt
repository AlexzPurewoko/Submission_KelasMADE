package id.apwdevs.app.catalogue.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import id.apwdevs.app.catalogue.plugin.PublicContract
import kotlinx.android.parcel.Parcelize

@Entity(tableName = PublicContract.DatabaseContract.TABLE_GENRES)
@Parcelize
data class GenreModel(

    @PrimaryKey
    @ColumnInfo(name = "id")
    @SerializedName("id")
    val id: Int,

    @ColumnInfo(name = "name")
    @SerializedName("name")
    var genreName: String?

) : Parcelable

data class GenreModelResponse(

    @SerializedName("genres")
    val allGenre: List<GenreModel>
) : ClassResponse
