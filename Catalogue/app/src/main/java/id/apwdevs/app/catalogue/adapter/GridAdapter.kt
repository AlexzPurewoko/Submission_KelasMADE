package id.apwdevs.app.catalogue.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
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

class GridAdapter<T : ResettableItem>(private val mContext: Context) : RecyclerView.Adapter<GridAdapter<T>.GridViewHolder<T>>(), Filterable {
    private var shortListModels: MutableList<T> = mutableListOf()
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

    fun resetAllData(shortListModels: MutableList<T>?) {
        if(shortListModels.isNullOrEmpty())return
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


    fun getItemData(postionData: Int): T? =
        when {
            shortListModels.isNullOrEmpty() || postionData < 0 || postionData >= shortListModels.size -> null
            else -> shortListModels[postionData]
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
            when(obj){
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

    inner class GridViewHolder<T : ResettableItem> internal constructor(private val view: View) : RecyclerView.ViewHolder(view) {

        private val moviePoster: ImageView = view.findViewById(R.id.item_list_image)
        private val title: TextView = view.findViewById(R.id.item_list_text_title)
        private val releaseDate: TextView = view.findViewById(R.id.item_list_release_date)
        private val ratingBar: RatingBar = view.findViewById(R.id.item_list_ratingBar)
        private val voteCount: TextView = view.findViewById(R.id.item_list_votecount)

        @SuppressLint("SetTextI18n")
        internal fun bind(dataModel: T) {
            when(dataModel){
                is MovieAboutModel -> {
                    title.text = dataModel.title
                    releaseDate.text = dataModel.releaseDate
                    ratingBar.rating = getRating(dataModel.voteAverage)
                    voteCount.text = "(${dataModel.voteCount})"
                    dataModel.posterPath?.let {
                        Glide.with(view.context)
                        .load(GetImageFiles.getImg(requestedWidth, it))
                        .apply(RequestOptions().override(requestedWidth, requestedHeight))
                        .into(moviePoster)
                    }

                }
                is TvAboutModel -> {
                    title.text = dataModel.name
                    releaseDate.text = dataModel.firstAirDate
                    ratingBar.rating = getRating(dataModel.voteAverage)
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
                                    Log.e("ErrorDisplayBitmap", anError?.errorBody, anError)
                                }

                            })
                    }
                }
            }

        }
        private fun getRating(originalRating: Double, stars: Int = 5, maxRating : Int = 10): Float = (originalRating * stars / maxRating).toFloat()

    }
}
