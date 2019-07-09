package id.apwdevs.app.catalogue.view

import id.apwdevs.app.catalogue.model.ResettableItem

interface MainListTvOrMovieView {
    fun onLoadData()
    fun <T : ResettableItem> onLoadFinished(data: List<T>, measuredMaxColumnCount: Int)
}