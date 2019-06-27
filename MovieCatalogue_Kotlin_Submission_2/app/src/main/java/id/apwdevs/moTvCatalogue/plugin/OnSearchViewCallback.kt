package id.apwdevs.moTvCatalogue.plugin

import android.support.v7.widget.SearchView


interface OnSearchViewCallback {
    fun onQueryTextChange(searchView: SearchView, strQuery: String?): Boolean
    fun onQueryTextSubmitted(searchView: SearchView, newText: String?): Boolean
    fun onSearchEnded(searchView: SearchView)
}