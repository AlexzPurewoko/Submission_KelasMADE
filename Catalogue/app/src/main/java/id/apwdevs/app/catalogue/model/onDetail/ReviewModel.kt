package id.apwdevs.app.catalogue.model.onDetail

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ReviewModel(
    val id: Int,
    var page: Int,
    var totalPages: Int,
    var totalResult: Int,
    val results: ArrayList<ReviewResultModel>
) : Parcelable

@Parcelize
data class ReviewResultModel(
    val id: String,
    val author: String,
    val content: String,
    val url: String
) : Parcelable
