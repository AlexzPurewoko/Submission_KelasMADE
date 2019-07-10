package id.apwdevs.moTvCatalogue.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.apwdevs.moTvCatalogue.R
import id.apwdevs.moTvCatalogue.model.OtherAboutFilmModel
import id.apwdevs.moTvCatalogue.model.OtherAboutTVModel
import java.util.*

class AboutAdapter(private val mContext: Context) : RecyclerView.Adapter<AboutAdapter.AboutDataViewHolder>() {
    private var aboutDataModels: MutableList<AboutDataModel> = ArrayList()

    fun setData(otherAboutFilmModel: OtherAboutFilmModel?) {
        if (otherAboutFilmModel == null) return
        if (aboutDataModels.size != 0) aboutDataModels.clear()
        aboutDataModels.add(
            AboutDataModel(
                mContext.getString(R.string.short_about_original_language),
                otherAboutFilmModel.originalLanguage
            )
        )
        aboutDataModels.add(
            AboutDataModel(
                mContext.getString(R.string.short_about_long_runtime),
                otherAboutFilmModel.longMovieRuntime
            )
        )
        aboutDataModels.add(
            AboutDataModel(
                mContext.getString(R.string.short_about_movie_budget),
                otherAboutFilmModel.movieBudget
            )
        )
        aboutDataModels.add(
            AboutDataModel(
                mContext.getString(R.string.short_about_revenue),
                otherAboutFilmModel.movieRevenue
            )
        )
        aboutDataModels.add(
            AboutDataModel(
                mContext.getString(R.string.short_about_genre),
                otherAboutFilmModel.movieGenres
            )
        )
    }

    fun setData(otherAboutTVModel: OtherAboutTVModel?) {
        if (otherAboutTVModel == null) return
        if (aboutDataModels.size != 0) aboutDataModels.clear()
        aboutDataModels.add(
            AboutDataModel(
                mContext.getString(R.string.short_about_original_language),
                otherAboutTVModel.originalLanguage
            )
        )
        aboutDataModels.add(
            AboutDataModel(
                mContext.getString(R.string.short_about_long_runtime),
                otherAboutTVModel.longTvRuntime
            )
        )
        aboutDataModels.add(AboutDataModel(mContext.getString(R.string.short_about_genre), otherAboutTVModel.tvGenres))
        aboutDataModels.add(AboutDataModel(mContext.getString(R.string.short_about_type), otherAboutTVModel.type))
        aboutDataModels.add(AboutDataModel(mContext.getString(R.string.short_about_status), otherAboutTVModel.tvStatus))
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): AboutDataViewHolder {
        val v = LayoutInflater.from(mContext).inflate(R.layout.item_side_left_about, viewGroup, false)
        return AboutDataViewHolder(v)
    }

    override fun onBindViewHolder(aboutDataViewHolder: AboutDataViewHolder, i: Int) {
        aboutDataViewHolder.bind(aboutDataModels[i])
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return aboutDataModels.size
    }

    inner class AboutDataViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {

        private val aboutTitle: TextView = view.findViewById(R.id.item_side_movie_title)
        private val aboutValue: TextView = view.findViewById(R.id.item_side_movie_query)

        internal fun bind(model: AboutDataModel) {
            aboutValue.text = model.aboutValue
            aboutTitle.text = model.aboutName
        }
    }

    data class AboutDataModel internal constructor(
        val aboutName: String?,
        val aboutValue: String?
    )
}