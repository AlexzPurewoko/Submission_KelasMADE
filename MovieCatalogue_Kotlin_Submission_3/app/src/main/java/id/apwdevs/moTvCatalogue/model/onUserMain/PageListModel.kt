package id.apwdevs.moTvCatalogue.model.onUserMain

import id.apwdevs.moTvCatalogue.model.ResettableItem

data class PageListModel(
    val inPage: Int,
    val totalResults: Int,
    val totalPages: Int,
    val contents: MutableList<out ResettableItem>?,
    val errorCode: Int,
    val errorMessage: String?
)