package id.apwdevs.moTvCatalogue.view

import id.apwdevs.moTvCatalogue.model.ResettableItem
import id.apwdevs.moTvCatalogue.model.ShortListModel
import id.apwdevs.moTvCatalogue.model.onUserMain.MovieAboutModel

interface MainListTvOrMovieView {
    fun onLoadData()
    fun <T : ResettableItem> onLoadFinished(data: List<T>, measuredMaxColumnCount: Int)
}