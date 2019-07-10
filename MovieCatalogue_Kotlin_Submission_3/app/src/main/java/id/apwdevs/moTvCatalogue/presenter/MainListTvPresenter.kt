package id.apwdevs.moTvCatalogue.presenter

import android.content.Context
import android.graphics.Point
import id.apwdevs.moTvCatalogue.R
import id.apwdevs.moTvCatalogue.model.ShortListModel
import id.apwdevs.moTvCatalogue.view.MainListTvOrMovieView
import java.util.*

class MainListTvPresenter(
    private val mContext: Context,
    private val view: MainListTvOrMovieView,
    private val windowSize: Point
) {

    fun prepareAll() {
        view.onLoadData()

        val resources = mContext.resources
        val arrTv = resources.getStringArray(R.array.tvshows_name_lists)
        val arrOverview = resources.getStringArray(R.array.tvshows_overview_lists)
        val arrReleased = resources.getStringArray(R.array.tvshows_released_time_lists)
        val arrMoviePoster = resources.obtainTypedArray(R.array.tvshows_drawable_lists)
        val listResult = ArrayList<ShortListModel>()
        for (x in arrTv.indices) {
            listResult.add(
                ShortListModel(
                    arrMoviePoster.getResourceId(x, -1),
                    arrOverview[x],
                    arrReleased[x],
                    arrTv[x]
                )
            )
        }
        arrMoviePoster.recycle()
        view.onLoadFinished(mutableListOf(), calculateMaxColumn())
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