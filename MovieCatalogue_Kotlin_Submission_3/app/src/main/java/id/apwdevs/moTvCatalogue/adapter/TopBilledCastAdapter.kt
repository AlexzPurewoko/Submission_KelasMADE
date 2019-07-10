package id.apwdevs.moTvCatalogue.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.apwdevs.moTvCatalogue.R
import id.apwdevs.moTvCatalogue.model.FilmTopBilledCastModel

class TopBilledCastAdapter(private val mContext: Context) :
    RecyclerView.Adapter<TopBilledCastAdapter.TopBilledCastViewHolder>() {
    var filmTopBilledCastModels: List<FilmTopBilledCastModel>? = null


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): TopBilledCastViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_list_actor, p0, false)
        return TopBilledCastViewHolder(view)
    }

    override fun getItemCount(): Int {
        return when (val itemCount = filmTopBilledCastModels?.size) {
            null -> 0
            else -> itemCount
        }
    }

    override fun onBindViewHolder(p0: TopBilledCastViewHolder, p1: Int) {
        val filmCastModel = filmTopBilledCastModels?.get(p1) ?: return
        p0.bind(filmCastModel)
    }

    inner class TopBilledCastViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.item_list_actor_name)
        private val alias: TextView = view.findViewById(R.id.item_list_actor_alias)

        internal fun bind(filmTopBilledCastModel: FilmTopBilledCastModel) {
            name.text = filmTopBilledCastModel.actorName
            alias.text = filmTopBilledCastModel.aliasPeople
        }
    }
}