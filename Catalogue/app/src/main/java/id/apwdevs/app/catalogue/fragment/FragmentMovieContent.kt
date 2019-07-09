package id.apwdevs.app.catalogue.fragment

import android.content.pm.ActivityInfo
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.adapter.GridAdapter
import id.apwdevs.app.catalogue.adapter.ListAdapter
import id.apwdevs.app.catalogue.model.onUserMain.MovieAboutModel
import id.apwdevs.app.catalogue.model.onUserMain.PageListModel
import id.apwdevs.app.catalogue.plugin.ItemClickSupport
import id.apwdevs.app.catalogue.plugin.PublicConfig
import id.apwdevs.app.catalogue.plugin.api.ApiRepository
import id.apwdevs.app.catalogue.plugin.calculateMaxColumn
import id.apwdevs.app.catalogue.plugin.view.ErrorSectionAdapter
import id.apwdevs.app.catalogue.plugin.view.SearchToolbarCard
import id.apwdevs.app.catalogue.view.MainUserListView
import id.apwdevs.app.catalogue.viewModel.MainListMovieViewModel
import id.apwdevs.app.catalogue.viewModel.MainListMovieViewModel.SupportedType.DISCOVER

class FragmentMovieContent: Fragment(), MainUserListView, SearchToolbarCard.OnSearchCallback {


    private lateinit var refreshPage : SwipeRefreshLayout
    private lateinit var errorLayout : LinearLayout
    private lateinit var recyclerContent : RecyclerView
    private lateinit var mainView: View

    private lateinit var viewModel : MainListMovieViewModel
    private lateinit var errorAdapter: ErrorSectionAdapter
    private lateinit var types : MainListMovieViewModel.SupportedType
    private lateinit var strTag : String
    private lateinit var recyclerListAdapter: ListAdapter<MovieAboutModel>
    private lateinit var recyclerGridAdapter: GridAdapter<MovieAboutModel>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fg_holder_content, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainView = view
        recyclerContent = view.findViewById(R.id.fg_content_recycler)
        refreshPage = view.findViewById(R.id.swipe_content_refresh)
        errorLayout = view.findViewById(R.id.error_section)
        errorAdapter = ErrorSectionAdapter(errorLayout)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recyclerGridAdapter = GridAdapter(requireContext())
        recyclerListAdapter = ListAdapter(requireContext())
        // we have to obtain a value of ViewModel
        viewModel = ViewModelProviders.of(this).get(MainListMovieViewModel::class.java)
        viewModel.dataListObj.observe(this, Observer {
            if(viewModel.hasFirstInstantiate.value == false)
                viewModel.hasFirstInstantiate.postValue(true)
            setupRecycler(it)
        })
        viewModel.currentListMode.observe(this, Observer {
            // update the layoutManager
            setupRecycler(viewModel.dataListObj.value)
        })

        types = arguments?.getParcelable(EXTRA_MOVIE_REQUESTED_TYPE) ?: DISCOVER
        strTag = "${FragmentMovieContent::class.java.simpleName}Type${types.name}"
        refreshPage.setOnRefreshListener {
            viewModel.setup(ApiRepository(), types, 1, strTag, this)
        }



        // change condition into swipeRefreshLayout to refresh the page if hasFirstInstantiate is false
        if(viewModel.hasFirstInstantiate.value == false) {
            viewModel.setup(ApiRepository(), types, 1, strTag, this)
            refreshPage.isRefreshing = true
        }

    }

    private fun setupRecycler(page: PageListModel<MovieAboutModel>?) {
        page?.let {
            // set the layoutManager and the adapter
            recyclerContent.apply {
                // find the windowSize
                val wSize = Point()
                requireActivity().windowManager.defaultDisplay.getSize(wSize)
                when (viewModel.currentListMode.value) {
                    PublicConfig.RecyclerMode.MODE_LIST -> {
                        layoutManager = LinearLayoutManager(context)
                        adapter = recyclerListAdapter
                        recyclerListAdapter.resetAllData(it.contents)
                    }
                    PublicConfig.RecyclerMode.MODE_GRID -> {
                        layoutManager = GridLayoutManager(context, calculateMaxColumn(context, wSize))
                        adapter = recyclerGridAdapter
                        recyclerGridAdapter.resetAllData(it.contents)
                    }
                    PublicConfig.RecyclerMode.MODE_STAGERRED_LIST -> {
                        layoutManager = StaggeredGridLayoutManager(calculateMaxColumn(context, wSize), ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        adapter = recyclerGridAdapter
                        recyclerGridAdapter.resetAllData(it.contents)
                    }
                }

                ItemClickSupport.addTo(this).onItemClickListener = object : ItemClickSupport.OnItemClickListener {
                    override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                        onRecyclerItemClicked(recyclerView, position, v)
                    }

                }

            }
        }
    }

    private fun onRecyclerItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
        Toast.makeText(requireContext(), "Clicked RecyclerView at position $position", Toast.LENGTH_SHORT).show()
    }

    //////////////// OVERRIDDEN FROM MainListUserView interfaces \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    override fun onLoadFinished() {
        refreshPage.isRefreshing = false
    }

    override fun onLoadSuccess(viewModel: ViewModel) {
        errorAdapter.unDisplayError()
        recyclerContent.apply {
            if(visibility == View.INVISIBLE)
                visibility = View.VISIBLE
        }

    }

    override fun onLoadFailed(err: ApiRepository.RetError) {
        recyclerContent.visibility = View.INVISIBLE
        errorAdapter.displayError(err)
    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////// OVERRIDDEN FROM OnSearchCallback \\\\\\\\\\\\\\\\\\\\\\\\\\\\

    override fun querySearch(view: View, query: CharSequence?, start: Int, before: Int, count: Int) {
            when(viewModel.currentListMode.value){
                PublicConfig.RecyclerMode.MODE_LIST -> {
                    recyclerListAdapter.filter.filter(query)
                }
                PublicConfig.RecyclerMode.MODE_GRID, PublicConfig.RecyclerMode.MODE_STAGERRED_LIST -> {
                    recyclerGridAdapter.filter.filter(query)
                }
            }
    }

    override fun onSubmit(query: String) {

    }

    override fun onSearchCancelled() {
        viewModel.dataListObj.value?.let {
            when(viewModel.currentListMode.value){
                PublicConfig.RecyclerMode.MODE_LIST -> {
                    recyclerListAdapter.resetAllData(it.contents)
                    recyclerListAdapter.notifyDataSetChanged()
                }
                PublicConfig.RecyclerMode.MODE_GRID, PublicConfig.RecyclerMode.MODE_STAGERRED_LIST -> {
                    recyclerGridAdapter.resetAllData(it.contents)
                    recyclerGridAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onTextCleared(searchHistory: String?) {
        onSearchCancelled()
    }

    override fun onSearchStarted() {

    }

    override fun onListModeChange(listMode: Int) {
        viewModel.currentListMode.postValue(listMode)
    }
    ///////////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    companion object {
        const val EXTRA_MOVIE_REQUESTED_TYPE = "MOVIE_REQ_TYPE"

        @JvmStatic
        internal fun newInstance(typeOfMovie: MainListMovieViewModel.SupportedType) : FragmentMovieContent =
            FragmentMovieContent().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_MOVIE_REQUESTED_TYPE, typeOfMovie)
                }
            }
    }
}