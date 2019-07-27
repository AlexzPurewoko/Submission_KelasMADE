package id.apwdevs.app.catalogue.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.entity.FavoriteEntity
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onUserMain.MovieAboutModel
import id.apwdevs.app.catalogue.model.onUserMain.TvAboutModel
import id.apwdevs.app.catalogue.plugin.SearchComponent
import id.apwdevs.app.catalogue.plugin.getBitmap

class GridAdapter<T : ResettableItem>(private val mContext: Context) :
    RecyclerView.Adapter<GridAdapter<T>.GridViewHolder<T>>(), Filterable {
    internal var shortListModels: MutableList<T> = mutableListOf()
    private val requestedWidth: Int = mContext.resources.getDimension(R.dimen.item_poster_width).toInt()
    private val requestedHeight: Int = mContext.resources.getDimension(R.dimen.item_poster_height).toInt()
    private val searchMethod = OnSearchMethod()

    init {
        setHasStableIds(true)
    }

    override fun getFilter(): Filter = searchMethod.filter

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): GridViewHolder<T> {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_grid_movies_or_tv, viewGroup, false)
        return GridViewHolder(view)
    }

    override fun onBindViewHolder(listViewHolder: GridViewHolder<T>, i: Int) {
        val shortListModel = shortListModels[i]
        listViewHolder.bind(shortListModel)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemCount(): Int {
        return shortListModels.size
    }

    fun resetAllData(shortListModels: List<T>?) {
        if (shortListModels.isNullOrEmpty()) return
        this.shortListModels.clear()
        for (model in shortListModels) {
            model.onReset()
        }
        this.shortListModels.addAll(shortListModels)
    }

    fun resetAllSpannables() {
        for (model in shortListModels) {
            model.onReset()
        }
    }

    private inner class OnSearchMethod : SearchComponent<T>() {
        override fun onSearchFinished(aList: MutableList<T>?) {
            aList?.let {
                shortListModels.clear()
                shortListModels.addAll(it)
                notifyDataSetChanged()
            }
        }

        override fun objectToBeSearch(): MutableList<T>? = shortListModels

        override fun compareObject(constraint: String, obj: T): Boolean {
            var str1: String? = null
            var str2: String? = null
            var spanned1: SpannableString? = null
            var spanned2: SpannableString? = null
            when (obj) {
                is MovieAboutModel -> {
                    str1 = obj.title
                    str2 = obj.releaseDate
                    spanned1 = obj.titleSpan
                    spanned2 = obj.releaseDateSpan
                }
                is TvAboutModel -> {
                    str1 = obj.name
                    str2 = obj.firstAirDate
                    spanned1 = obj.nameSpan
                    spanned2 = obj.firstAirDateSpan
                }
                is FavoriteEntity -> {
                    str1 = obj.title
                    str2 = obj.releaseDate
                    spanned1 = obj.titleSpan
                    spanned2 = obj.releaseDateSpan
                }
            }
            obj.onReset()
            if ((!str1.isNullOrEmpty() && str1.contains(
                    constraint,
                    true
                )) || (!str2.isNullOrEmpty() && str2.contains(
                    constraint,
                    true
                ))
            ) {
                val matchStr1 = getItemMatchedPosition(constraint, str1, true)
                val matchStr2 = getItemMatchedPosition(constraint, str2, true)
                matchStr1.forEach {
                    spanned1?.setSpan(
                        ForegroundColorSpan(Color.RED),
                        it.startPosition,
                        it.endPosition,
                        0
                    )
                }
                matchStr2.forEach {
                    spanned2?.setSpan(
                        StyleSpan(Typeface.BOLD),
                        it.startPosition,
                        it.endPosition,
                        0
                    )
                }
                return true
            }
            return false
        }

    }

    inner class GridViewHolder<T : ResettableItem> internal constructor(view: View) :
        RecyclerView.ViewHolder(view) {

        private val poster: ImageView = view.findViewById(R.id.item_poster_image)
        private val title: TextView = view.findViewById(R.id.item_list_text_title)
        private val releaseDate: TextView = view.findViewById(R.id.item_list_release_date)
        private val ratingBar: RatingBar = view.findViewById(R.id.item_list_ratingBar)
        private val voteCount: TextView = view.findViewById(R.id.item_list_votecount)

        @SuppressLint("SetTextI18n")
        internal fun bind(dataModel: T) {
            when (dataModel) {
                is MovieAboutModel -> {
                    title.text = dataModel.title
                    releaseDate.text = dataModel.releaseDate
                    ratingBar.rating = getRating(dataModel.voteAverage)
                    voteCount.text = "(${dataModel.voteCount})"
                    dataModel.posterPath?.let {
                        getBitmap(Point(requestedWidth, requestedHeight), it) { bitmap ->
                            poster.setImageBitmap(bitmap)
                        }
                    }

                }
                is TvAboutModel -> {
                    title.text = dataModel.name
                    releaseDate.text = dataModel.firstAirDate
                    ratingBar.rating = getRating(dataModel.voteAverage)
                    voteCount.text = "(${dataModel.voteCount})"
                    dataModel.posterPath?.let {
                        getBitmap(Point(requestedWidth, requestedHeight), it) { bitmap ->
                            poster.setImageBitmap(bitmap)
                        }
                    }
                }
                is FavoriteEntity -> {
                    title.text = dataModel.title
                    releaseDate.text = dataModel.releaseDate
                    ratingBar.rating = getRating(dataModel.voteAverage)
                    voteCount.text = "(${dataModel.voteCount})"
                    dataModel.posterPath?.let {
                        getBitmap(Point(requestedWidth, requestedHeight), it) { bitmap ->
                            poster.setImageBitmap(bitmap)
                        }
                    }

                }
            }

        }

        private fun getRating(originalRating: Double, stars: Int = 5, maxRating: Int = 10): Float =
            (originalRating * stars / maxRating).toFloat()

    }
}
