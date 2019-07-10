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
import id.apwdevs.moTvCatalogue.model.onUserMain.TvAboutModel
import id.apwdevs.moTvCatalogue.plugin.ItemClickSupport
import id.apwdevs.moTvCatalogue.plugin.OnListModeChanged
import id.apwdevs.moTvCatalogue.plugin.OnSearchViewCallback
import id.apwdevs.moTvCatalogue.presenter.MainListTvPresenter
import id.apwdevs.moTvCatalogue.view.MainListTvOrMovieView
import java.util.*

class TvViewListFragment : Fragment(), MainListTvOrMovieView, OnListModeChanged, OnSearchViewCallback {

    private var mListTvRecycler: RecyclerView? = null
    private var mTextStateSearch: TextView? = null
    private var tvListAdapter: ListAdapter<TvAboutModel>? = null
    private var tvGridAdapter: GridAdapter<TvAboutModel>? = null
    private var data: List<TvAboutModel>? = null
    private var measuredColumn = 1
    private var currentMode = MainUserActivity.MODE_LIST


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_movie_or_tv_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mListTvRecycler = view.findViewById(R.id.list_movies_recycler)
        mTextStateSearch = view.findViewById(R.id.text_state_search)

    }

    private fun setRecyclerList() {
        mListTvRecycler?.apply {
            tvListAdapter = ListAdapter(context)
            tvListAdapter?.resetAllData(data as ArrayList<TvAboutModel>)
            adapter = tvListAdapter
            layoutManager = LinearLayoutManager(context)
            ItemClickSupport.addTo(this).onItemClickListener = object : ItemClickSupport.OnItemClickListener {
                override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                    // resets the adapter
                    tvListAdapter?.resetAllSpannables()
                    startActivity(
                        Intent(context, DetailMovieOrTv::class.java).apply {
                            putExtra(DetailMovieOrTv.EXTRA_MOVIE_OR_TV_NUM, position)
                            putExtra(DetailMovieOrTv.EXTRA_MOVIE_OR_TV_DATA, tvListAdapter?.getItemData(position) as TvAboutModel)
                            putExtra(DetailMovieOrTv.EXTRA_MODES, DetailMovieOrTv.MODE_TV)
                        }
                    )
                }
            }
        }
    }

    private fun setRecyclerGrid(mode: Int) {
        mListTvRecycler?.apply {
            tvGridAdapter = GridAdapter(requireContext())
            tvGridAdapter?.resetAllData(data as ArrayList<TvAboutModel>)
            adapter = tvGridAdapter
            layoutManager =
                when (mode) {
                    MainUserActivity.MODE_GRID -> GridLayoutManager(context, measuredColumn)
                    else -> StaggeredGridLayoutManager(measuredColumn, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                }

            ItemClickSupport.addTo(this).onItemClickListener = object :
                ItemClickSupport.OnItemClickListener {
                override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                    // resets the adapter
                    tvGridAdapter?.resetAllSpannables()
                    startActivity(
                        Intent(context, DetailMovieOrTv::class.java).apply {
                            putExtra(DetailMovieOrTv.EXTRA_MOVIE_OR_TV_NUM, position)
                            putExtra(DetailMovieOrTv.EXTRA_MOVIE_OR_TV_DATA, tvGridAdapter?.getItemData(position) as TvAboutModel?)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            currentMode = savedInstanceState.getInt(EXTRA_LIST_MODE)
        }
        val wSize = Point()
        requireActivity().windowManager.defaultDisplay.getSize(wSize)
        val mainListMoviePresenter = MainListTvPresenter(requireContext(), this, wSize)
        mainListMoviePresenter.prepareAll()
    }

    override fun onDetach() {
        super.onDetach()
        mListTvRecycler?.let {
            ItemClickSupport.removeFrom(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(EXTRA_LIST_MODE, currentMode)
    }

    override fun onLoadData() {

    }

    override fun <T : ResettableItem> onLoadFinished(data: List<T>, measuredMaxColumnCount: Int) {

        this.data = data as List<TvAboutModel>
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
                tvGridAdapter?.resetAllData(data?.toMutableList())
                if (!strQuery.isNullOrEmpty())
                    tvGridAdapter?.filter?.filter(strQuery)
            }
            MainUserActivity.MODE_LIST -> {
                tvListAdapter?.resetAllData(data as ArrayList<TvAboutModel>)
                if (!strQuery.isNullOrEmpty())
                    tvListAdapter?.filter?.filter(strQuery)
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
                tvGridAdapter?.resetAllData(data as ArrayList<TvAboutModel>)
            }
            MainUserActivity.MODE_LIST -> {
                tvListAdapter?.resetAllData(data as ArrayList<TvAboutModel>)
            }
        }
        mTextStateSearch?.text = ""
        mTextStateSearch?.visibility = View.GONE
    }

    companion object {
        private const val EXTRA_LIST_MODE = "CURRENT_MODE"
    }
}