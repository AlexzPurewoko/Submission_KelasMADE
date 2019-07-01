package id.apwdevs.moTvCatalogue.model.onUserMain

import id.apwdevs.moTvCatalogue.model.ResettableItem

data class PageListModel(
    var inPage: Int,
    var totalResults: Int,
    var totalPages: Int,
    val contents: MutableList<ResettableItem>?,
    var errorCode: Int,
    var errorMessage: String?
)