package id.apwdevs.app.catalogue.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.entity.FavoriteEntity
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onUserMain.MainDataItemModel
import id.apwdevs.app.catalogue.plugin.SearchComponent
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer
import id.apwdevs.app.catalogue.viewModel.MainListViewModel

class GridAdapter<T : ResettableItem>(
    private val mContext: Context,
    private val mItemOptions: MainListViewModel.ItemCardOptions,
    private val coloredTextState: Boolean,
    private val wSize: String
) :
    RecyclerView.Adapter<GridAdapter<T>.GridViewHolder<T>>(), Filterable {
    internal var shortListModels: MutableList<T> = mutableListOf()
    private val requestedWidth: Int = mContext.resources.getDimension(R.dimen.item_poster_width).toInt()
    private val requestedHeight: Int = mContext.resources.getDimension(R.dimen.item_poster_height).toInt()
    private val searchMethod = OnSearchMethod()
    var notifyDataSetsChange: NotifyDataSetsChange? = null
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
        notifyDataSetsChange?.onDataChange(shortListModels.isEmpty(), shortListModels)
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
                notifyDataSetsChange?.onDataChange(it.isEmpty(), it)
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
                is MainDataItemModel -> {
                    str1 = obj.title
                    str2 = obj.releaseDate
                    spanned1 = obj.titleSpan
                    spanned2 = obj.releaseDateSpan
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
                if (coloredTextState) {
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
                }
                return true
            }
            return false
        }

    }

    inner class GridViewHolder<T : ResettableItem> internal constructor(view: View) :
        RecyclerView.ViewHolder(view) {

        private val poster: ImageView = view.findViewById(R.id.item_poster_image)
        private val titleView: TextView = view.findViewById(R.id.item_list_text_title)
        private val releaseDateTxt: TextView = view.findViewById(R.id.item_list_release_date)
        private val ratingBar: RatingBar = view.findViewById(R.id.item_list_ratingBar)
        private val voteCountTxt: TextView = view.findViewById(R.id.item_list_votecount)
        private val backdrop: ImageView = view.findViewById(R.id.item_image_backdrop)
        private val mWorker = GetObjectFromServer.getInstance(itemView.context)

        @SuppressLint("SetTextI18n")
        internal fun bind(dataModel: T) {
            val title: CharSequence?
            val voteAverage: Double
            val voteCount: Int
            val posterPath: String?
            val backdropPath: String?
            val releaseDate: String?
            when (dataModel) {
                is MainDataItemModel -> {
                    title = dataModel.titleSpan ?: dataModel.title
                    voteAverage = dataModel.voteAverage
                    voteCount = dataModel.voteCount
                    posterPath = dataModel.posterPath
                    backdropPath = dataModel.backdropPath
                    releaseDate = dataModel.releaseDate
                }
                is FavoriteEntity -> {
                    title = dataModel.titleSpan ?: dataModel.title
                    voteAverage = dataModel.voteAverage
                    voteCount = dataModel.voteCount
                    posterPath = dataModel.posterPath
                    backdropPath = dataModel.backdropPath
                    releaseDate = dataModel.releaseDate
                }
                else -> return
            }

            val cardColor: Int = mItemOptions.cardColor
            val imageTintColor: Int = mItemOptions.imageTintColor
            val tintMode: PorterDuff.Mode = mItemOptions.tintMode
            val itemColor: Int = mItemOptions.itemColor
            if (itemColor != MainListViewModel.DEFAULT_COLOR) {
                titleView.setTextColor(itemColor)
                voteCountTxt.setTextColor(itemColor)
                releaseDateTxt.setTextColor(itemColor)
            }
            (itemView as CardView).setCardBackgroundColor(cardColor)
            backdrop.imageTintMode = tintMode
            backdrop.imageTintList = ColorStateList.valueOf(imageTintColor)

            titleView.text = title
            ratingBar.rating = getRating(voteAverage)
            voteCountTxt.text = "($voteCount)"
            releaseDateTxt.text = releaseDate
            posterPath?.let {
                mWorker.getBitmapNoProgress(Point(requestedWidth, requestedHeight), it, true) { bitmap ->
                    poster.setImageBitmap(bitmap)
                }
            }
            if (mItemOptions != MainListViewModel.ItemCardOptions.DARK && mItemOptions != MainListViewModel.ItemCardOptions.LIGHT)
                backdropPath?.let {
                    val p = Point(if (wSize.equals("original", true)) 1000 else wSize.toInt(), 0)
                    mWorker.getBitmapNoProgress(p, it) { bitmap ->
                        backdrop.scaleType = ImageView.ScaleType.CENTER_CROP
                        backdrop.setImageBitmap(bitmap)
                    }
                }
        }

        private fun getRating(originalRating: Double, stars: Int = 5, maxRating: Int = 10): Float =
            (originalRating * stars / maxRating).toFloat()

    }
}
