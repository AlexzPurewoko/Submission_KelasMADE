package id.apwdevs.app.catalogue.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.model.GenreModel
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onDetail.OtherMovieAboutModel
import id.apwdevs.app.catalogue.model.onDetail.OtherTVAboutModel
import id.apwdevs.app.catalogue.model.onUserMain.MainDataItemModel
import id.apwdevs.app.catalogue.plugin.getCurrency
import id.apwdevs.app.catalogue.plugin.getReadableTime
import id.apwdevs.app.catalogue.plugin.view.WrappedView
import id.apwdevs.app.catalogue.repository.onDetail.DetailActivityRepository
import java.util.*

class RecyclerAboutAdapter(
    private val context: Context,
    private val shortDetail: ResettableItem,
    private val otherDetail: ResettableItem,
    private val type: DetailActivityRepository.TypeContentContract
) : RecyclerView.Adapter<RecyclerAboutAdapter.RecyclerAboutVH>() {

    private val listToBeAdded: MutableList<Item> = mutableListOf()

    init {
        setData()
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long =
        position.toLong()

    override fun getItemViewType(position: Int): Int = position


    private fun setData() {

        val det1 = shortDetail as MainDataItemModel
        listToBeAdded.apply {
            add(Item(context.getString(R.string.original_title), det1.originalTitle))
            add(Item(context.getString(R.string.original_language), Locale(det1.originalLanguage).displayLanguage))
            add(Item(context.getString(R.string.genre), det1.actualGenreModel))
        }
        when (type) {
            DetailActivityRepository.TypeContentContract.TV_SHOWS -> {
                val det2 = otherDetail as OtherTVAboutModel
                listToBeAdded.apply {
                    add(Item(context.getString(R.string.first_air_date), det1.releaseDate))
                    add(Item(context.getString(R.string.last_air_date), det2.lastAirDate))
                    add(Item(context.getString(R.string.status), det2.status))
                    add(Item(context.getString(R.string.number_of_episodes), det2.numberOfEpisodes))
                    add(Item(context.getString(R.string.number_of_seasons), det2.numberOfSeasons))
                    add(Item(context.getString(R.string.type), det2.type))
                    add(Item(context.getString(R.string.in_production), det2.inProduction))
                    add(Item(context.getString(R.string.origin_country), det2.originCountry))
                    add(Item(context.getString(R.string.homepage), det2.homepage, true))
                }
            }
            DetailActivityRepository.TypeContentContract.MOVIE -> {
                val det2 = otherDetail as OtherMovieAboutModel
                listToBeAdded.apply {
                    add(Item(context.getString(R.string.release_date), det1.releaseDate))
                    add(Item(context.getString(R.string.runtime), getReadableTime(det2.runtime)))
                    add(Item(context.getString(R.string.status), det2.status))
                    add(Item(context.getString(R.string.budget), getCurrency("$", det2.movieBudget.toString())))
                    add(Item(context.getString(R.string.revenue), getCurrency("$", det2.revenue.toString())))
                    add(Item(context.getString(R.string.homepage), det2.homepage, true))
                    add(Item(context.getString(R.string.tagline), det2.tagLine))
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAboutVH =
        RecyclerAboutVH(LayoutInflater.from(parent.context).inflate(R.layout.adapter_item_about, parent, false))

    override fun getItemCount(): Int = listToBeAdded.size

    override fun onBindViewHolder(holder: RecyclerAboutVH, position: Int) {
        holder.bind(listToBeAdded[position], position)
    }

    data class Item(
        val title: String,
        val values: Any?,
        var asLink: Boolean = false
    )

    class RecyclerAboutVH(view: View) : RecyclerView.ViewHolder(view) {
        private val mTitle: TextView = view.findViewById(R.id.adapter_about_text_title)
        private val wrapView: WrappedView = view.findViewById(R.id.adapter_about_contents)

        fun bind(item: Item, position: Int) {
            mTitle.text = item.title
            getValuesAndAppendIntoLayout(item.values, item)
            //if odd, we change into right position
            if (position % 2 == 0) {
                if (itemView is LinearLayout)
                    itemView.gravity = Gravity.END
            } else {

                if (itemView is LinearLayout)
                    itemView.gravity = Gravity.START
            }
        }

        private fun getValuesAndAppendIntoLayout(anyVal: Any?, item: Item) {
            if (anyVal == null) {
                wrapView.addText("-")
                return
            }
            when (anyVal) {
                is CharSequence? -> {
                    wrapView.addText(anyVal)
                }
                is String? -> {
                    wrapView.addText(if (anyVal.isEmpty()) "-" else anyVal, item.asLink)
                }
                is Int? -> {
                    wrapView.addText(anyVal.toString())
                }
                is List<*> -> {
                    anyVal.forEach {
                        if (it is GenreModel)
                            wrapView.addText(it.genreName)
                        else if (it is String)
                            wrapView.addText(it)
                    }
                }
                is Boolean? -> {
                    wrapView.addText(itemView.context.getString(if (anyVal) R.string.true_text else R.string.false_text))
                }
            }
        }
    }
}