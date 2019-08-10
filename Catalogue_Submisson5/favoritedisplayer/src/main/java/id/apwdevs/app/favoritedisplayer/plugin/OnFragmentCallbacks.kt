package id.apwdevs.app.favoritedisplayer.plugin

import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

interface OnFragmentCallbacks {
    fun onItemClicked(fg: Fragment, recyclerView: RecyclerView, position: Int, v: View)
    fun onRequestRefresh(fragment: Fragment)
    fun onFavoriteChanged()
}