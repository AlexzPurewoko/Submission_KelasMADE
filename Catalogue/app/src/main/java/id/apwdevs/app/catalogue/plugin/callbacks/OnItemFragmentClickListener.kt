package id.apwdevs.app.catalogue.plugin.callbacks

import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

interface OnItemFragmentClickListener {
    fun onItemClicked(fg: Fragment, recyclerView: RecyclerView, position: Int, v: View)
}