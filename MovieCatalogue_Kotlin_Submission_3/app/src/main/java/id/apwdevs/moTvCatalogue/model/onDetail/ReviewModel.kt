package id.apwdevs.moTvCatalogue.model.onDetail

data class ReviewModel(
    val id: Int,
    var page: Int,
    var totalPages: Int,
    var totalResult: Int,
    val results: MutableList<ReviewResultModel>
)

data class ReviewResultModel(
    val id: String,
    val author: String,
    val content: String,
    val url: String
)
