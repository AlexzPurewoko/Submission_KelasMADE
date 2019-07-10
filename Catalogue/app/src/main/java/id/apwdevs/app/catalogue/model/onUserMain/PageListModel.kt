package id.apwdevs.app.catalogue.model.onUserMain

import id.apwdevs.app.catalogue.model.ResettableItem

data class PageListModel<T : ResettableItem>(
    val inPage: Int,
    val totalResults: Int,
    val totalPages: Int,
    val contents: ArrayList<T>?,
    val errorCode: Int,
    val errorMessage: String?
)