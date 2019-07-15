package id.apwdevs.app.catalogue.fragment

import android.content.pm.ActivityInfo
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
import id.apwdevs.app.catalogue.plugin.OnItemFragmentClickListener
import id.apwdevs.app.catalogue.plugin.PublicConfig
import id.apwdevs.app.catalogue.plugin.api.ApiRepository
import id.apwdevs.app.catalogue.plugin.calculateMaxColumn
import id.apwdevs.app.catalogue.plugin.view.ErrorSectionAdapter
import id.apwdevs.app.catalogue.plugin.view.SearchToolbarCard
import id.apwdevs.app.catalogue.view.MainUserListView
import id.apwdevs.app.catalogue.viewModel.MainListMovieViewModel

class FragmentMovieContent : Fragment(), MainUserListView, SearchToolbarCard.OnSearchCallback, OnSelectedFragment {


    private lateinit var refreshPage: SwipeRefreshLayout
    private lateinit var errorLayout: LinearLayout
    private lateinit var recyclerContent: RecyclerView
    private lateinit var mainView: View

    internal var viewModel: MainListMovieViewModel? = null
    private lateinit var errorAdapter: ErrorSectionAdapter
    private lateinit var types: MainListMovieViewModel.SupportedType
    private lateinit var strTag: String
    private lateinit var recyclerListAdapter: ListAdapter<MovieAboutModel>
    private lateinit var recyclerGridAdapter: GridAdapter<MovieAboutModel>

    var onItemClickListener: OnItemFragmentClickListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerGridAdapter = GridAdapter(requireContext())
        recyclerListAdapter = ListAdapter(requireContext())
        types = requireNotNull(arguments).getParcelable(EXTRA_MOVIE_REQUESTED_TYPE)
        if (savedInstanceState == null) {

        } else {
            recyclerListAdapter.restoreOldData(savedInstanceState.getParcelableArrayList("ListDataMovie${types.name}"))
        }
    }

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

        // we have to obtain a value of ViewModel
        initViewModel()

        strTag = "${FragmentMovieContent::class.java.simpleName}Type${types.name}"
        refreshPage.setOnRefreshListener {
            viewModel?.setup(ApiRepository(), types, 1, strTag, this)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("ListDataMovie${types.name}", recyclerListAdapter.dataModel)
    }

    private fun initViewModel() {
        //if(viewModel != null) return
        viewModel = ViewModelProviders.of(this).get(MainListMovieViewModel::class.java).apply {
            dataListObj.observe(this@FragmentMovieContent, Observer {
                if (hasFirstInstantiate.value == false)
                    hasFirstInstantiate.postValue(true)
                setupRecycler(it)
            })
            currentListMode.observe(this@FragmentMovieContent, Observer {
                // update the layoutManager
                setupRecycler(dataListObj.value)
            })
            fragmentIsRefreshing.observe(this@FragmentMovieContent, Observer {
                refreshPage.isRefreshing = it
            })
        }
    }

    private fun setupRecycler(page: PageListModel<MovieAboutModel>?) {
        page?.let {
            // set the layoutManager and the adapter
            recyclerContent.apply {
                // find the windowSize
                val wSize = Point()
                requireActivity().windowManager.defaultDisplay.getSize(wSize)
                when (viewModel?.currentListMode?.value) {
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
                        layoutManager = StaggeredGridLayoutManager(
                            calculateMaxColumn(context, wSize),
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        )
                        adapter = recyclerGridAdapter
                        recyclerGridAdapter.resetAllData(it.contents)
                    }
                }

                ItemClickSupport.addTo(this)?.onItemClickListener = object : ItemClickSupport.OnItemClickListener {
                    override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                        onRecyclerItemClicked(recyclerView, position, v)
                    }

                }

            }
        }
    }

    private fun onRecyclerItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
        onItemClickListener?.onItemClicked(this, recyclerView, position, v)
    }

    //////////////// OVERRIDDEN FROM MainListUserView interfaces \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    override fun onLoadFinished() {
        viewModel?.fragmentIsRefreshing?.postValue(false)
    }

    override fun onLoadSuccess(viewModel: ViewModel) {
        errorAdapter.unDisplayError()
        recyclerContent.apply {
            if (visibility == View.INVISIBLE)
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
        when (viewModel?.currentListMode?.value) {
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
        viewModel?.dataListObj?.value?.let {
            when (viewModel?.currentListMode?.value) {
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
        viewModel?.currentListMode?.postValue(listMode)
    }
    ///////////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    //////////////////////////////////////  OVERRIDDEN FROM OnSelectedFragment \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    override fun start(fragment: Fragment, position: Int) {
        Log.e("Start Fragment", "FragmentStart ${fragment.javaClass.simpleName}")
        //initViewModel()
        viewModel?.let {
            if (it.hasFirstInstantiate.value == false) {
                it.setup(ApiRepository(), types, 1, strTag, this)
                it.fragmentIsRefreshing.value = true
            }
        }


    }
    ////////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    companion object {
        const val EXTRA_MOVIE_REQUESTED_TYPE = "MOVIE_REQ_TYPE"

        @JvmStatic
        internal fun newInstance(typeOfMovie: MainListMovieViewModel.SupportedType): FragmentMovieContent =
            FragmentMovieContent().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_MOVIE_REQUESTED_TYPE, typeOfMovie)
                }
            }
    }
}