package id.apwdevs.app.catalogue.adapter

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.clearSpans
import androidx.recyclerview.widget.RecyclerView
import id.apwdevs.app.catalogue.R

class PageIndicatorAdapter : RecyclerView.Adapter<PageIndicatorAdapter.PageIndicatorHolder>() {

    var availableHolder: MutableSet<PageIndicatorHolder> = mutableSetOf()
    var markCurrentSelectedPage: Int = 1
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
        holder.textIndicator.text = SpannableString("${position + 1}").apply {
            if (markCurrentSelectedPage == position + 1)
                setSpan(StyleSpan(Typeface.BOLD), 0, length, 0)
            else
                clearSpans()
        }
        availableHolder.add(holder)
    }

    override fun onViewRecycled(holder: PageIndicatorHolder) {
        availableHolder.remove(holder)
    }

    class PageIndicatorHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textIndicator: TextView = view.findViewById(R.id.adapter_page_indicator_text)

    }

}