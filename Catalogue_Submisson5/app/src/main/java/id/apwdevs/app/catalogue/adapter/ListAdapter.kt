package id.apwdevs.app.catalogue.adapter

import android.annotation.SuppressLint
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
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.entity.FavoriteEntity
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onUserMain.MainDataItemModel
import id.apwdevs.app.catalogue.plugin.SearchComponent
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer
import id.apwdevs.app.catalogue.plugin.configureFavorite
import id.apwdevs.app.catalogue.plugin.view.WrappedView
import id.apwdevs.app.catalogue.viewModel.MainListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class ListAdapter<T : ResettableItem>(
    private val mContext: AppCompatActivity,
    private val mItemOptions: MainListViewModel.ItemCardOptions,
    private val coloredTextState: Boolean,
    private val wSize: String,
    private var reqRefreshRootDataSets: () -> Unit = {}
) :
    RecyclerView.Adapter<ListAdapter<T>.ListViewHolder<T>>(), Filterable {
    var dataModel: ArrayList<T> = arrayListOf()
    private val requestedWidth: Int = mContext.resources.getDimension(R.dimen.item_poster_width).toInt()
    private val requestedHeight: Int = mContext.resources.getDimension(R.dimen.item_poster_height).toInt()
    private val searchMethod = OnSearchMethod()
    var notifyDataSetsChange: NotifyDataSetsChange? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemViewType(position: Int): Int = position
    override fun getFilter(): Filter = searchMethod.filter

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ListViewHolder<T> {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_list_movies_or_tv, viewGroup, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(listViewHolder: ListViewHolder<T>, i: Int) {
        val shortListModel = dataModel[i]
        listViewHolder.bind(shortListModel)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return dataModel.size
    }

    fun resetAllSpannables() {
        for (model in dataModel) {
            model.onReset()
        }
    }

    fun resetAllData(newDataModel: List<T>?) {
        newDataModel?.let {
            dataModel.clear()
            dataModel.addAll(it)
            notifyDataSetsChange?.onDataChange(it.isEmpty(), it)
            resetAllSpannables()
        }
    }

    private inner class OnSearchMethod : SearchComponent<T>() {
        override fun onSearchFinished(aList: MutableList<T>?) {
            aList?.let {
                dataModel.clear()
                dataModel.addAll(it)
                notifyDataSetsChange?.onDataChange(it.isEmpty(), it)
                notifyDataSetChanged()
            }
        }

        override fun objectToBeSearch(): MutableList<T>? = dataModel

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

    inner class ListViewHolder<T : ResettableItem> internal constructor(view: View) :
        RecyclerView.ViewHolder(view), View.OnClickListener {

        private val poster: ImageView = view.findViewById(R.id.item_poster_image)
        private val titleView: TextView = view.findViewById(R.id.item_list_text_title)
        private val overviewTxt: TextView = view.findViewById(R.id.item_list_overview)
        private val rating: RatingBar = view.findViewById(R.id.item_list_ratingBar)
        private val voteCountTxt: TextView = view.findViewById(R.id.item_list_votecount)
        private val itemGenres: WrappedView = view.findViewById(R.id.item_list_genres)
        private val itemFavorites: ImageView = view.findViewById(R.id.item_list_btn_favorite)
        private val backdrop: ImageView = view.findViewById(R.id.item_image_backdrop)
        private val mWorker = GetObjectFromServer.getInstance(itemView.context)

        private var tmpModel: T? = null

        @SuppressLint("SetTextI18n")
        internal fun bind(dataModel: T) {
            tmpModel = dataModel
            val title: CharSequence?
            val overview: CharSequence?
            val voteAverage: Double
            val voteCount: Int
            val posterPath: String?
            val backdropPath: String?
            when (dataModel) {
                is MainDataItemModel -> {
                    title = dataModel.titleSpan ?: dataModel.title
                    overview = dataModel.overview
                    voteAverage = dataModel.voteAverage
                    voteCount = dataModel.voteCount
                    posterPath = dataModel.posterPath
                    backdropPath = dataModel.backdropPath
                    setFavorites(dataModel.isFavorite)
                    if (itemGenres.childCount == 0)
                        dataModel.actualGenreModel?.forEach { actGenre ->
                            itemGenres.addText(actGenre.genreName)
                        }
                }
                is FavoriteEntity -> {
                    title = dataModel.titleSpan ?: dataModel.title
                    overview = dataModel.overview
                    voteAverage = dataModel.voteAverage
                    voteCount = dataModel.voteCount
                    posterPath = dataModel.posterPath
                    backdropPath = dataModel.backdropPath
                    setFavorites(true)
                    if (itemGenres.childCount == 0)
                        dataModel.genreIds?.split(",")?.forEach { actGenre ->
                            itemGenres.addText(actGenre)
                        }
                }
                else -> return
            }

            val cardColor: Int = mItemOptions.cardColor
            val imageTintColor: Int = mItemOptions.imageTintColor
            val tintMode: PorterDuff.Mode = mItemOptions.tintMode
            val itemColor: Int = mItemOptions.itemColor
            if (itemColor != MainListViewModel.DEFAULT_COLOR) {
                titleView.setTextColor(itemColor)
                overviewTxt.setTextColor(itemColor)
                voteCountTxt.setTextColor(itemColor)
            }

            (itemView as CardView).setCardBackgroundColor(cardColor)
            backdrop.imageTintMode = tintMode
            backdrop.imageTintList = ColorStateList.valueOf(imageTintColor)

            titleView.text = title
            overviewTxt.text = overview
            rating.rating = getRating(voteAverage)
            voteCountTxt.text = "($voteCount)"
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
            itemFavorites.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (v?.id == itemFavorites.id) {
                GlobalScope.launch(Dispatchers.IO) {
                    val fav = configureFavorite(itemView.context, tmpModel)
                    if (tmpModel is FavoriteEntity) {
                        mContext.runOnUiThread {
                            reqRefreshRootDataSets()
                        }
                    } else {
                        itemFavorites.post {
                            setFavorites(fav)
                            Snackbar.make(
                                itemView, when (fav) {
                                    true -> itemFavorites.context.getString(R.string.add_fav)
                                    false -> itemFavorites.context.getString(R.string.remove_fav)
                                }, Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }


        private fun setFavorites(enabled: Boolean) {
            itemFavorites.setImageResource(
                if (enabled) {
                    itemFavorites.tag = REMOVE_FAVORITE
                    R.drawable.ic_favorite_activated_24dp
                } else {
                    itemFavorites.tag = ADD_FAVORITE
                    R.drawable.ic_favorite_border_24dp
                }
            )
        }

        private fun getRating(originalRating: Double, stars: Int = 5, maxRating: Int = 10): Float =
            (originalRating * stars / maxRating).toFloat()


    }

    companion object {
        const val ADD_FAVORITE = "ADD_FAVORITE"
        const val REMOVE_FAVORITE = "REMOVE_FAVORITE"
    }
}

interface NotifyDataSetsChange {
    fun onDataChange(isEmptyData: Boolean, listData: List<Any>)
}