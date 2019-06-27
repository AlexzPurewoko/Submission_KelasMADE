package id.apwdevs.moTvCatalogue.presenter

import android.content.Context
import android.graphics.Point
import id.apwdevs.moTvCatalogue.R
import id.apwdevs.moTvCatalogue.model.ShortListModel
import id.apwdevs.moTvCatalogue.view.MainListTvOrMovieView
import java.util.*

class MainListMoviePresenter(
    private val mContext: Context,
    private val view: MainListTvOrMovieView,
    private val windowSize: Point
) {

    fun prepareAll() {
        view.onLoadData()

        val resources = mContext.resources
        val arrMovies = resources.getStringArray(R.array.movie_name_list)
        val arrOverview = resources.getStringArray(R.array.movie_overview_str_list)
        val arrReleased = resources.getStringArray(R.array.movie_released_time_list)
        val arrMoviePoster = resources.obtainTypedArray(R.array.movie_drawable_list)
        val listResult = ArrayList<ShortListModel>()
        for (x in arrMovies.indices) {
            listResult.add(
                ShortListModel(
                    arrMoviePoster.getResourceId(x, -1),
                    arrOverview[x],
                    arrReleased[x],
                    arrMovies[x]
                )
            )
        }
        arrMoviePoster.recycle()
        view.onLoadFinished(listResult, calculateMaxColumn())
    }

    private fun calculateMaxColumn(): Int {
        val resources = mContext.resources
        val vPagerMarginStart = resources.getDimension(R.dimen.viewpager_user_marginstart)
        val vPagerMarginEnd = resources.getDimension(R.dimen.viewpager_user_marginstart)
        val cardViewPadding = resources.getDimension(R.dimen.cardview_grid_content_padding)
        val imageViewWidth = resources.getDimension(R.dimen.item_poster_width)

        var maxColumn = 1
        while (true) {
            val contentMeasuredWidth = imageViewWidth + 2 * cardViewPadding
            val spaceFreeForContent =
                windowSize.x.toFloat() - (vPagerMarginStart + vPagerMarginEnd) - maxColumn * contentMeasuredWidth
            if (spaceFreeForContent > contentMeasuredWidth) {
                maxColumn++
            } else {
                break
            }
        }
        return maxColumn
    }
}