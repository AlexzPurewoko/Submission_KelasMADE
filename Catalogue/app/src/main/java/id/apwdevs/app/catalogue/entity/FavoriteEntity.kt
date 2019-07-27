package id.apwdevs.app.catalogue.entity

import android.os.Parcelable
import android.text.SpannableString
import androidx.core.text.clearSpans
import androidx.room.ColumnInfo
import androidx.room.ColumnInfo.INTEGER
import androidx.room.ColumnInfo.REAL
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import id.apwdevs.app.catalogue.model.ClassResponse
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.plugin.PublicContract
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Entity(tableName = PublicContract.DatabaseContract.TABLE_FAVORITES)
@Parcelize
data class FavoriteEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "contentType", typeAffinity = INTEGER)
    val contentType: Int,

    @ColumnInfo(name = "releaseDate")
    val releaseDate: String,

    @ColumnInfo(name = "overview")
    val overview: String?,

    @ColumnInfo(name = "genreIds")
    val genreIds: String?,

    @ColumnInfo(name = "posterPath")
    val posterPath: String?,

    @ColumnInfo(name = "voteCount", typeAffinity = INTEGER, defaultValue = "0")
    val voteCount: Int,

    @ColumnInfo(name = "voteAverage", typeAffinity = REAL, defaultValue = "0")
    val voteAverage: Double


) : Parcelable, ResettableItem {

    @Ignore
    @IgnoredOnParcel
    var titleSpan: SpannableString? = null

    @Ignore
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

data class FavoriteResponse(
    val listAll: List<FavoriteEntity>
) : ClassResponse