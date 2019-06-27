package id.apwdevs.moTvCatalogue.adapter

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import id.apwdevs.moTvCatalogue.R
import id.apwdevs.moTvCatalogue.adapter.GridAdapter.ListViewHolder
import id.apwdevs.moTvCatalogue.model.ShortListModel

class GridAdapter(private val mContext: Context) : RecyclerView.Adapter<ListViewHolder>(), Filterable {
    private var shortListModels: MutableList<ShortListModel> = mutableListOf()
    private val requestedWidth: Int = mContext.resources.getDimension(R.dimen.item_poster_width).toInt()
    private val requestedHeight: Int = mContext.resources.getDimension(R.dimen.item_poster_height).toInt()

    init {
        setHasStableIds(true)
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            if (shortListModels.isEmpty()) return FilterResults()
            val charString = constraint.toString()
            val newLists: MutableList<ShortListModel> = if (charString.isEmpty()) shortListModels else {
                val filteredList = mutableListOf<ShortListModel>()
                for (modelData in shortListModels) {
                    modelData.resetSpannableString()
                    val str1 = modelData.title
                    val str2 = modelData.releaseDate
                    if ((!str1.isNullOrEmpty() && str1.contains(
                            charString,
                            true
                        )) || (!str2.isNullOrEmpty() && str2.contains(
                            charString,
                            true
                        ))
                    ) {
                        modelData.title = setSpannable(charString, str1, true)
                        modelData.releaseDate = setSpannable(charString, str2, true)
                        filteredList.add(modelData)
                    }
                }
                filteredList
            }
            val filterResults = FilterResults()
            filterResults.values = newLists
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results?.values != null) {
                shortListModels.clear()
                shortListModels.addAll(results.values as MutableList<ShortListModel>)
                notifyDataSetChanged()
            }
        }

        private fun setSpannable(
            comparatorString: CharSequence,
            source: CharSequence?,
            ignoreCase: Boolean
        ): SpannableString {
            if (source == null) return SpannableString("")
            var index = 0
            val spannableString = SpannableString(source)
            while (true) {
                var startPos = -1
                var endPos: Int
                var countEqual = 0
                for (compChar in comparatorString) {
                    if (index == source.length) break
                    val baseChar = source[index++]
                    if (compChar.equals(baseChar, ignoreCase)) {
                        if (startPos == -1) {
                            startPos = index - 1
                        }
                        countEqual++
                    } else break
                }
                // if match with length, add the spannable strings
                if (countEqual == comparatorString.length) {
                    endPos = index
                    spannableString.setSpan(BackgroundColorSpan(Color.YELLOW), startPos, endPos, 0)
                }
                if (index == source.length) break
            }
            return spannableString
        }

    }

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ListViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_grid_movies_or_tv, viewGroup, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(listViewHolder: ListViewHolder, i: Int) {
        val shortListModel = shortListModels[i]
        listViewHolder.bind(shortListModel)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemCount(): Int {
        return shortListModels.size
    }

    fun resetAllData(shortListModels: MutableList<ShortListModel>) {
        this.shortListModels.clear()
        for (model in shortListModels) {
            model.resetSpannableString()
        }
        this.shortListModels.addAll(shortListModels)
    }

    fun resetAllSpannables() {
        for (model in shortListModels) {
            model.resetSpannableString()
        }
    }


    fun getItemData(postionData: Int): ShortListModel? =
        when {
            shortListModels.isNullOrEmpty() || postionData < 0 || postionData >= shortListModels.size -> null
            else -> shortListModels[postionData]
        }


    inner class ListViewHolder internal constructor(private val view: View) : RecyclerView.ViewHolder(view) {

        private val moviePoster: ImageView = view.findViewById(R.id.item_list_image)
        private val title: TextView = view.findViewById(R.id.item_list_text_title)
        private val releaseDate: TextView = view.findViewById(R.id.item_list_release_date)

        internal fun bind(shortListModel: ShortListModel) {
            title.text = shortListModel.title
            releaseDate.text = shortListModel.releaseDate
            Glide.with(view.context)
                .load(shortListModel.photoRes)
                .apply(RequestOptions().override(requestedWidth, requestedHeight))
                .into(moviePoster)
        }

    }
}
