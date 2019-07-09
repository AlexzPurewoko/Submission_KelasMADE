package id.apwdevs.app.catalogue.plugin

import android.content.Context
import android.graphics.Point
import android.view.View
import id.apwdevs.app.catalogue.R


fun jsonCheckAndGet(get: Any): Any? =
    if (get.toString() == "null") null
    else get

fun View.visible() {
    visibility = View.VISIBLE
}
fun View.gone() {
    visibility = View.GONE
}
fun View.invisible() {
    visibility = View.INVISIBLE
}

fun calculateMaxColumn(mContext: Context, windowSize: Point): Int {
    val resources = mContext.resources
    val vPagerMarginStart = resources.getDimension(R.dimen.viewpager_user_marginstart)
    val vPagerMarginEnd = resources.getDimension(R.dimen.viewpager_user_marginend)
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