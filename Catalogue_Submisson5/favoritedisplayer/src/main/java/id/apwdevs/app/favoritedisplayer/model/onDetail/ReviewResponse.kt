package id.apwdevs.app.favoritedisplayer.model.onDetail

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import id.apwdevs.app.favoritedisplayer.model.ClassResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ReviewResponse(

    @SerializedName("id")
    val id: Int,

    @SerializedName("page")
    var page: Int,

    @SerializedName("total_pages")
    var totalPages: Int,

    @SerializedName("total_results")
    var totalResult: Int,

    @SerializedName("results")
    val results: List<ReviewResultModel>
) : Parcelable, ClassResponse

@Parcelize
data class ReviewResultModel(

    @SerializedName("id")
    val id: String,

    @SerializedName("author")
    val author: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("url")
    val url: String
) : Parcelable
