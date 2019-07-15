package id.apwdevs.app.catalogue.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.BitmapRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onUserMain.MovieAboutModel
import id.apwdevs.app.catalogue.model.onUserMain.TvAboutModel
import id.apwdevs.app.catalogue.plugin.SearchComponent
import id.apwdevs.app.catalogue.plugin.api.GetImageFiles
import id.apwdevs.app.catalogue.plugin.view.WrappedView
import java.util.*

class ListAdapter<T : ResettableItem>(private val mContext: Context) :
    RecyclerView.Adapter<ListAdapter<T>.ListViewHolder<T>>(), Filterable {
    var dataModel: ArrayList<T> = arrayListOf()
    private val requestedWidth: Int = mContext.resources.getDimension(R.dimen.item_poster_width).toInt()
    private val requestedHeight: Int = mContext.resources.getDimension(R.dimen.item_poster_height).toInt()
    private val searchMethod = OnSearchMethod()


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

    fun resetAllData(newDataModel: ArrayList<T>?) {
        newDataModel?.let {
            dataModel.clear()
            dataModel.addAll(it)
            resetAllSpannables()
        }
    }

    fun restoreOldData(oldData: ArrayList<T>?) {
        oldData?.let {
            dataModel.clear()
            dataModel.addAll(it)
            notifyDataSetChanged()
        }
    }

    fun getItemData(postionData: Int): ResettableItem? =
        when {
            dataModel.isNullOrEmpty() || postionData < 0 || postionData >= dataModel.size -> null
            else -> dataModel[postionData]
        }

    private inner class OnSearchMethod : SearchComponent<T>() {
        override fun onSearchFinished(aList: MutableList<T>?) {
            aList?.let {
                dataModel.clear()
                dataModel.addAll(it)
                notifyDataSetChanged()
            }
        }

        override fun objectToBeSearch(): MutableList<T>? = dataModel

        override fun compareObject(constraint: String, obj: T): Boolean {
            when (obj) {
                is MovieAboutModel -> {
                    val str1 = obj.title
                    val str2 = obj.releaseDate
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
                        val spanned1 = SpannableString(str1)
                        val spanned2 = SpannableString(str2)
                        matchStr1.forEach {
                            spanned1.setSpan(
                                ForegroundColorSpan(Color.RED),
                                it.startPosition,
                                it.endPosition,
                                0
                            )
                        }
                        matchStr2.forEach {
                            spanned2.setSpan(
                                StyleSpan(Typeface.BOLD),
                                it.startPosition,
                                it.endPosition,
                                0
                            )
                        }
                        obj.title = spanned1
                        obj.releaseDate = spanned2
                        return true
                    }
                }
                is TvAboutModel -> {
                    val str1 = obj.name
                    val str2 = obj.firstAirDate
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
                        val spanned1 = SpannableString(str1)
                        val spanned2 = SpannableString(str2)
                        matchStr1.forEach {
                            spanned1.setSpan(
                                ForegroundColorSpan(Color.RED),
                                it.startPosition,
                                it.endPosition,
                                0
                            )
                        }
                        matchStr2.forEach {
                            spanned2.setSpan(
                                StyleSpan(Typeface.BOLD),
                                it.startPosition,
                                it.endPosition,
                                0
                            )
                        }
                        obj.name = spanned1
                        obj.firstAirDate = spanned2
                        return true
                    }
                }
            }
            return false
        }

    }

    inner class ListViewHolder<T : ResettableItem> internal constructor(private val view: View) :
        RecyclerView.ViewHolder(view) {

        private val moviePoster: ImageView = view.findViewById(R.id.item_poster_image)
        private val title: TextView = view.findViewById(R.id.item_list_text_title)
        private val overview: TextView = view.findViewById(R.id.item_list_overview)
        private val rating: RatingBar = view.findViewById(R.id.item_list_ratingBar)
        private val voteCount: TextView = view.findViewById(R.id.item_list_votecount)
        private val itemGenres: WrappedView = view.findViewById(R.id.item_list_genres)

        @SuppressLint("SetTextI18n")
        internal fun bind(dataModel: T) {
            when (dataModel) {
                is MovieAboutModel -> {
                    title.text = dataModel.title
                    overview.text = dataModel.overview
                    rating.rating = getRating(dataModel.voteAverage)
                    voteCount.text = "(${dataModel.voteCount})"
                    dataModel.posterPath?.let {
                        AndroidNetworking.get(GetImageFiles.getImg(requestedWidth, it))
                            .setPriority(Priority.LOW)
                            .setBitmapMaxHeight(requestedHeight)
                            .setBitmapMaxWidth(requestedWidth)
                            .setImageScaleType(ImageView.ScaleType.FIT_XY)
                            .build()
                            .getAsBitmap(object : BitmapRequestListener {
                                override fun onResponse(response: Bitmap?) {
                                    moviePoster.setImageBitmap(response)
                                }

                                override fun onError(anError: ANError?) {

                                }

                            })
                    }

                    if (itemGenres.childCount == 0) {
                        dataModel.genres?.forEach {
                            itemGenres.addText(it.genreName)
                        }
                    }
                }
                is TvAboutModel -> {
                    title.text = dataModel.name
                    overview.text = dataModel.overview
                    rating.rating = getRating(dataModel.voteAverage)
                    voteCount.text = "(${dataModel.voteCount})"
                    dataModel.posterPath?.let {
                        Glide.with(view.context)
                            .load(GetImageFiles.getImg(requestedWidth, it))
                            .apply(RequestOptions().override(requestedWidth, requestedHeight))
                            .into(moviePoster)
                    }
                    if (itemGenres.childCount == 0) {
                        dataModel.genres?.forEach {
                            itemGenres.addText(it.genreName)
                        }
                    }
                }

            }

        }

        private fun getRating(originalRating: Double, stars: Int = 5, maxRating: Int = 10): Float =
            (originalRating * stars / maxRating).toFloat()
    }
}