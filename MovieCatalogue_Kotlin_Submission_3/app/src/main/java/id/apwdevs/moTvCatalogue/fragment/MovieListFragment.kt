package id.apwdevs.moTvCatalogue.fragment

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import id.apwdevs.moTvCatalogue.R
import id.apwdevs.moTvCatalogue.activities.DetailMovieOrTv
import id.apwdevs.moTvCatalogue.activities.MainUserActivity
import id.apwdevs.moTvCatalogue.adapter.GridAdapter
import id.apwdevs.moTvCatalogue.adapter.ListAdapter
import id.apwdevs.moTvCatalogue.model.ResettableItem
import id.apwdevs.moTvCatalogue.model.ShortListModel
import id.apwdevs.moTvCatalogue.model.onUserMain.MovieAboutModel
import id.apwdevs.moTvCatalogue.plugin.ItemClickSupport
import id.apwdevs.moTvCatalogue.plugin.OnListModeChanged
import id.apwdevs.moTvCatalogue.plugin.OnSearchViewCallback
import id.apwdevs.moTvCatalogue.presenter.MainListMoviePresenter
import id.apwdevs.moTvCatalogue.view.MainListTvOrMovieView
import java.util.*


class MovieListFragment : Fragment(), MainListTvOrMovieView, OnListModeChanged, OnSearchViewCallback {

    private var mListMovieRecycler: RecyclerView? = null
    private var mTextStateSearch: TextView? = null
    private var listAdapter: ListAdapter<MovieAboutModel>? = null
    private var gridAdapter: GridAdapter<MovieAboutModel>? = null
    private var data: List<MovieAboutModel>? = null
    private var measuredColumn = 1
    private var currentMode = MainUserActivity.MODE_LIST


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_movie_or_tv_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mListMovieRecycler = view.findViewById(R.id.list_movies_recycler)
        mTextStateSearch = view.findViewById(R.id.text_state_search)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            currentMode = savedInstanceState.getInt(EXTRA_LIST_MODE)
        }
        val wSize = Point()
        activity?.windowManager?.defaultDisplay?.getSize(wSize)
        MainListMoviePresenter(requireContext(), this, wSize).prepareAll()
    }

    override fun onDetach() {
        super.onDetach()
        mListMovieRecycler?.let {
            ItemClickSupport.removeFrom(it)
        }
    }

    private fun setRecyclerList() {
        mListMovieRecycler?.apply {
            listAdapter = ListAdapter(context)
            listAdapter?.resetAllData(data!! as ArrayList<MovieAboutModel>)
            adapter = listAdapter
            layoutManager = LinearLayoutManager(context)
            ItemClickSupport.addTo(this).onItemClickListener = object : ItemClickSupport.OnItemClickListener {
                override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                    // resets the adapter
                    listAdapter?.resetAllSpannables()
                    startActivity(
                        Intent(context, DetailMovieOrTv::class.java).apply {
                            putExtra(DetailMovieOrTv.EXTRA_MOVIE_OR_TV_NUM, position)
                            putExtra(DetailMovieOrTv.EXTRA_MOVIE_OR_TV_DATA, listAdapter?.getItemData(position) as MovieAboutModel?)
                            putExtra(DetailMovieOrTv.EXTRA_MODES, DetailMovieOrTv.MODE_MOVIE)
                        }
                    )
                }
            }
        }
    }

    private fun setRecyclerGrid(mode: Int) {
        mListMovieRecycler?.apply {
            gridAdapter = GridAdapter(requireContext())
            gridAdapter?.resetAllData(data!! as ArrayList<MovieAboutModel>)
            adapter = gridAdapter
            layoutManager =
                when (mode) {
                    MainUserActivity.MODE_GRID -> GridLayoutManager(context, measuredColumn)
                    else -> StaggeredGridLayoutManager(measuredColumn, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                }

            ItemClickSupport.addTo(this).onItemClickListener = object :
                ItemClickSupport.OnItemClickListener {
                override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                    // resets the adapter
                    gridAdapter?.resetAllSpannables()
                    startActivity(
                        Intent(context, DetailMovieOrTv::class.java).apply {
                            putExtra(DetailMovieOrTv.EXTRA_MOVIE_OR_TV_NUM, position)
                            putExtra(DetailMovieOrTv.EXTRA_MOVIE_OR_TV_DATA, gridAdapter?.getItemData(position))
                            putExtra(DetailMovieOrTv.EXTRA_MODES, DetailMovieOrTv.MODE_TV)
                        }
                    )
                }
            }
        }
    }

    private fun setRecycler() {
        when (currentMode) {
            MainUserActivity.MODE_GRID -> setRecyclerGrid(MainUserActivity.MODE_GRID)
            MainUserActivity.MODE_LIST -> setRecyclerList()
            MainUserActivity.MODE_STAGERRED_LIST -> setRecyclerGrid(MainUserActivity.MODE_STAGERRED_LIST)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(EXTRA_LIST_MODE, currentMode)
    }

    override fun onLoadData() {

    }

    override fun <T : ResettableItem> onLoadFinished(data: List<T>, measuredMaxColumnCount: Int) {
        this.data = data as List<MovieAboutModel>
        measuredColumn = measuredMaxColumnCount
        setRecycler()
    }

    override fun onListModeChanged(currentListMode: Int) {
        currentMode = currentListMode
        setRecycler()
    }

    override fun onQueryTextChange(searchView: SearchView, strQuery: String?): Boolean {

        // notify the users for text that they search
        val strRes = getString(R.string.search_result_text)
        val concatStr = "$strRes $strQuery"
        val spannedString = SpannableString(concatStr)
        spannedString.setSpan(StyleSpan(Typeface.BOLD_ITALIC), strRes.length, concatStr.length, 0)
        mTextStateSearch?.visibility = View.VISIBLE
        mTextStateSearch?.text = spannedString
        when (currentMode) {
            MainUserActivity.MODE_GRID, MainUserActivity.MODE_STAGERRED_LIST -> {
                gridAdapter?.resetAllData(data as ArrayList<MovieAboutModel>)
                if (!strQuery.isNullOrEmpty())
                    gridAdapter?.filter?.filter(strQuery)
            }
            MainUserActivity.MODE_LIST -> {
                listAdapter?.resetAllData(data as ArrayList<MovieAboutModel>)
                if (!strQuery.isNullOrEmpty())
                    listAdapter?.filter?.filter(strQuery)
            }
        }
        return true
    }

    override fun onQueryTextSubmitted(searchView: SearchView, newText: String?): Boolean {
        return true
    }

    override fun onSearchEnded(searchView: SearchView) {
        when (currentMode) {
            MainUserActivity.MODE_GRID, MainUserActivity.MODE_STAGERRED_LIST -> {
                gridAdapter?.resetAllData(data as ArrayList<MovieAboutModel>)
            }
            MainUserActivity.MODE_LIST -> {
                listAdapter?.resetAllData(data as ArrayList<MovieAboutModel>)
            }
        }
        mTextStateSearch?.text = ""
        mTextStateSearch?.visibility = View.GONE
    }

    companion object {
        private const val EXTRA_LIST_MODE = "CURRENT_MODE"
    }
}