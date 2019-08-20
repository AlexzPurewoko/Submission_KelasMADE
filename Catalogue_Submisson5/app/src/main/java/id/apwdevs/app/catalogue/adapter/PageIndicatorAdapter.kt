package id.apwdevs.app.catalogue.adapter

import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.apwdevs.app.catalogue.R

class PageIndicatorAdapter : RecyclerView.Adapter<PageIndicatorAdapter.PageIndicatorHolder>() {

    var markCurrentSelectedPage: Int = 0
    var pageLength: Int = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageIndicatorHolder =
        PageIndicatorHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_page_indicator_item, parent, false)
        )

    override fun getItemCount(): Int = pageLength

    override fun onBindViewHolder(holder: PageIndicatorHolder, position: Int) {
        val spanString = SpannableString("$position")
        holder.textIndicator.text = "$position"
    }

    class PageIndicatorHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textIndicator: TextView = view.findViewById(R.id.adapter_page_indicator_text)
    }

}