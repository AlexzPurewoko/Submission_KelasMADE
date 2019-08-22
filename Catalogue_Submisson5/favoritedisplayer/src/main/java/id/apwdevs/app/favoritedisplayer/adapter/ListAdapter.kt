package id.apwdevs.app.favoritedisplayer.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import id.apwdevs.app.favoritedisplayer.R
import id.apwdevs.app.favoritedisplayer.model.FavoriteEntity
import id.apwdevs.app.favoritedisplayer.plugin.Contracts
import id.apwdevs.app.favoritedisplayer.plugin.GetImageFiles
import id.apwdevs.app.favoritedisplayer.plugin.WrappedView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class ListAdapter(
    private val mContext: AppCompatActivity,
    private var callbacks: (onSuccessChangeFav: Boolean) -> Unit = {}
) :
    RecyclerView.Adapter<ListAdapter.ListViewHolder>() {
    var dataModel: ArrayList<FavoriteEntity> = arrayListOf()
    private val requestedWidth: Int =
        mContext.resources.getDimension(R.dimen.item_poster_width).toInt()
    private val requestedHeight: Int =
        mContext.resources.getDimension(R.dimen.item_poster_height).toInt()

    init {
        setHasStableIds(true)
    }

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ListViewHolder {
        val view =
            LayoutInflater.from(mContext).inflate(R.layout.item_list_movies_or_tv, viewGroup, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(listViewHolder: ListViewHolder, i: Int) {
        val shortListModel = dataModel[i]
        listViewHolder.bind(shortListModel)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return dataModel.size
    }

    private fun resetAllSpannables() {
        for (model in dataModel) {
            model.onReset()
        }
    }

    fun resetAllData(newDataModel: List<FavoriteEntity>?) {
        newDataModel?.let {
            dataModel.clear()
            dataModel.addAll(it)
            resetAllSpannables()
        }
    }

    inner class ListViewHolder internal constructor(view: View) :
        RecyclerView.ViewHolder(view), View.OnClickListener {

        private val poster: ImageView = view.findViewById(R.id.item_poster_image)
        private val titleView: TextView = view.findViewById(R.id.item_list_text_title)
        private val overviewTxt: TextView = view.findViewById(R.id.item_list_overview)
        private val rating: RatingBar = view.findViewById(R.id.item_list_ratingBar)
        private val voteCountTxt: TextView = view.findViewById(R.id.item_list_votecount)
        private val itemGenres: WrappedView = view.findViewById(R.id.item_list_genres)
        private val itemFavorites: ImageView = view.findViewById(R.id.item_list_btn_favorite)

        private var tmpModel: FavoriteEntity? = null

        @SuppressLint("SetTextI18n")
        internal fun bind(dataModel: FavoriteEntity) {
            tmpModel = dataModel
            val title: CharSequence? = dataModel.titleSpan ?: dataModel.title
            val overview: CharSequence? = dataModel.overview
            val voteAverage: Double = dataModel.voteAverage
            val voteCount: Int = dataModel.voteCount
            val posterPath: String? = dataModel.posterPath
            setFavorites(true)
            if (itemGenres.childCount == 0)
                dataModel.genreIds?.split(",")?.forEach { actGenre ->
                    itemGenres.addText(actGenre)
                }

            titleView.text = title
            overviewTxt.text = overview
            rating.rating = getRating(voteAverage)
            voteCountTxt.text = "($voteCount)"
            posterPath?.let {
                Glide.with(itemView.context.applicationContext)
                    .load(GetImageFiles.getImg(requestedWidth, it))
                    .apply(RequestOptions().override(requestedWidth, requestedHeight))
                    .into(poster)


            }
            itemFavorites.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (v?.id == itemFavorites.id) {
                GlobalScope.launch(Dispatchers.IO) {
                    val fav = Contracts.removeFromFavorite(itemView.context, tmpModel?.id ?: -1)
                    callbacks(fav >= 0)
                }
            }
        }


        private fun setFavorites(@Suppress("SameParameterValue") enabled: Boolean) {
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