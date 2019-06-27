package id.apwdevs.moTvCatalogue.view

import id.apwdevs.moTvCatalogue.model.ShortListModel

interface MainListTvOrMovieView {
    fun onLoadData()
    fun onLoadFinished(data: List<ShortListModel>, measuredMaxColumnCount: Int)
}