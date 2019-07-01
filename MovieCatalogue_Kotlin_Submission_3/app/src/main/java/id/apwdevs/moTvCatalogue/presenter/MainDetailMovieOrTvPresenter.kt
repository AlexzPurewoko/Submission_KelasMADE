package id.apwdevs.moTvCatalogue.presenter

import android.content.Context
import id.apwdevs.moTvCatalogue.R
import id.apwdevs.moTvCatalogue.activities.DetailMovieOrTv
import id.apwdevs.moTvCatalogue.model.OtherAboutFilmModel
import id.apwdevs.moTvCatalogue.model.OtherAboutTVModel
import id.apwdevs.moTvCatalogue.view.MainDetailMovieView

class MainDetailMovieOrTvPresenter(
    private val mContext: Context,
    private val mainDetailMovieView: MainDetailMovieView
) {

    fun prepareAll(position: Int, classMode: Int) {
        mainDetailMovieView.onLoadData()
        val data = mContext.resources
        var result: Any? = null
        when (classMode) {
            DetailMovieOrTv.MODE_MOVIE -> {
                val arrShortAboutList = data.getStringArray(R.array.movie_short_about_list)
                val arrTopBilledCast = data.getStringArray(R.array.movie_top_billed_cast_list)

                val otherAboutFilmModel = OtherAboutFilmModel(arrShortAboutList[position])
                otherAboutFilmModel.setListTopBilledCast(arrTopBilledCast[position])
                result = otherAboutFilmModel
            }
            DetailMovieOrTv.MODE_TV -> {
                val arrShortAbout = data.getStringArray(R.array.tvshows_short_about_lists)
                val otherAboutTVModel = OtherAboutTVModel(arrShortAbout[position])
                result = otherAboutTVModel
            }
        }
        mainDetailMovieView.onLoadFinished(result)
    }
}
