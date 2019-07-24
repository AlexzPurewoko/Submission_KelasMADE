package id.apwdevs.app.catalogue.fragment

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
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
import id.apwdevs.app.catalogue.activities.GetFromHostActivity
import id.apwdevs.app.catalogue.adapter.GridAdapter
import id.apwdevs.app.catalogue.adapter.ListAdapter
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onUserMain.PageListModel
import id.apwdevs.app.catalogue.plugin.*
import id.apwdevs.app.catalogue.plugin.api.ApiRepository
import id.apwdevs.app.catalogue.plugin.callbacks.OnItemFragmentClickListener
import id.apwdevs.app.catalogue.plugin.callbacks.OnSelectedFragment
import id.apwdevs.app.catalogue.plugin.view.ErrorSectionAdapter
import id.apwdevs.app.catalogue.plugin.view.SearchToolbarCard
import id.apwdevs.app.catalogue.view.MainUserListView
import id.apwdevs.app.catalogue.viewModel.MainListMovieViewModel
import id.apwdevs.app.catalogue.viewModel.MainListTvViewModel
import id.apwdevs.app.catalogue.viewModel.MainListViewModel

class FragmentContents : Fragment(), MainUserListView, SearchToolbarCard.OnSearchCallback, OnSelectedFragment {

    private lateinit var refreshPage: SwipeRefreshLayout
    private lateinit var errorLayout: ScrollView
    private lateinit var recyclerContent: RecyclerView
    private lateinit var mainView: View

    internal var viewModel: MainListViewModel? = null
    private lateinit var errorAdapter: ErrorSectionAdapter
    private lateinit var types: PublicConfig.ContentDisplayType
    private lateinit var contentReqTypes: Parcelable // order in MainListMovieModel and MainListTvModel
    private lateinit var strTag: String
    private lateinit var recyclerListAdapter: ListAdapter<ResettableItem>
    private lateinit var recyclerGridAdapter: GridAdapter<ResettableItem>
    private var mRequestIntoHostActivity: GetFromHostActivity? = null

    var onItemClickListener: OnItemFragmentClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerGridAdapter = GridAdapter(requireContext())
        recyclerListAdapter = ListAdapter(requireContext())
        arguments?.apply {
            types = getParcelable(EXTRA_CONTENT_TYPES)
            when (types) {
                PublicConfig.ContentDisplayType.MOVIE -> contentReqTypes =
                    getParcelable<MainListMovieViewModel.SupportedType>(EXTRA_CONTENT_REQUESTED_TYPES)
                PublicConfig.ContentDisplayType.TV_SHOWS -> contentReqTypes =
                    getParcelable<MainListTvViewModel.SupportedType>(EXTRA_CONTENT_REQUESTED_TYPES)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(
            R.layout.fg_holder_content,
            container,
            false
        )

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

        strTag = "${FragmentContents::class.java.simpleName}Type${types.name}ContentType$contentReqTypes"
        // we have to obtain a value of ViewModel
        initViewModel()
        refreshPage.setOnRefreshListener {
            viewModel?.getAt(contentReqTypes, 1)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mRequestIntoHostActivity = context as GetFromHostActivity
        } catch (e: ClassCastException) {
            Log.e("FragmentContents", "You Must Implement GetFromHostActivity to continue")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mRequestIntoHostActivity = null
    }

    private fun initViewModel() {
        viewModel = when (types) {
            PublicConfig.ContentDisplayType.TV_SHOWS -> {
                ViewModelProviders.of(this).get(MainListTvViewModel::class.java)
            }
            PublicConfig.ContentDisplayType.MOVIE -> {
                ViewModelProviders.of(this).get(MainListMovieViewModel::class.java)
            }
        }.apply {
            setup(requireContext(), this@FragmentContents, strTag)
            dataListObj.observe(this@FragmentContents, Observer {
                setupRecycler(it, prevListMode.value ?: 0)
            })
            prevListMode.observe(this@FragmentContents, Observer {
                setupRecycler(dataListObj.value, it)
            })
            fragmentIsRefreshing.observe(this@FragmentContents, Observer {
                refreshPage.isRefreshing = it
            })

        }
    }

    private fun setupRecycler(page: PageListModel<ResettableItem>?, listMode: Int) {
        page?.let {
            // set the layoutManager and the adapter
            recyclerContent.apply {
                // find the windowSize
                val wSize = Point()
                requireActivity().windowManager.defaultDisplay.getSize(wSize)
                ItemClickSupport.removeFrom(this)
                when (listMode) {
                    PublicConfig.RecyclerMode.MODE_LIST -> {
                        layoutManager = LinearLayoutManager(context)
                        adapter = recyclerListAdapter
                        recyclerListAdapter.resetAllData(it.contents)
                        recyclerListAdapter.notifyDataSetChanged()
                    }
                    PublicConfig.RecyclerMode.MODE_GRID -> {
                        layoutManager = GridLayoutManager(context, calculateMaxColumn(context, wSize))
                        adapter = recyclerGridAdapter
                        recyclerGridAdapter.resetAllData(it.contents)
                        recyclerGridAdapter.notifyDataSetChanged()
                    }
                    PublicConfig.RecyclerMode.MODE_STAGERRED_LIST -> {
                        layoutManager = StaggeredGridLayoutManager(
                            calculateMaxColumn(context, wSize),
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        )
                        adapter = recyclerGridAdapter
                        recyclerGridAdapter.resetAllData(it.contents)
                        recyclerGridAdapter.notifyDataSetChanged()
                    }
                }
                requestFocusFromTouch()

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
            if (!isVisible) visible()
        }

    }

    override fun onLoadFailed(err: ApiRepository.RetError) {
        recyclerContent.invisible()
        errorAdapter.displayError(err)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////// OVERRIDDEN FROM OnSearchCallback \\\\\\\\\\\\\\\\\\\\\\\\\\\\

    override fun querySearch(view: View, query: CharSequence?, start: Int, before: Int, count: Int) {
        when (viewModel?.prevListMode?.value) {
            PublicConfig.RecyclerMode.MODE_LIST -> {
                recyclerListAdapter.resetAllData(viewModel?.dataListObj?.value?.contents)
                if (query?.isNotBlank() == true)
                    recyclerListAdapter.filter.filter(query)
            }
            PublicConfig.RecyclerMode.MODE_GRID, PublicConfig.RecyclerMode.MODE_STAGERRED_LIST -> {
                recyclerGridAdapter.resetAllData(viewModel?.dataListObj?.value?.contents)
                if (query?.isNotBlank() == true)
                    recyclerGridAdapter.filter.filter(query)
            }
        }
    }

    override fun onSubmit(query: String) {

    }

    override fun onSearchCancelled() {
        viewModel?.let {
            val contents = it.dataListObj.value?.contents
            when (it.prevListMode.value) {
                PublicConfig.RecyclerMode.MODE_LIST -> {
                    recyclerListAdapter.resetAllData(contents)
                    recyclerListAdapter.notifyDataSetChanged()
                }
                PublicConfig.RecyclerMode.MODE_GRID, PublicConfig.RecyclerMode.MODE_STAGERRED_LIST -> {
                    recyclerGridAdapter.resetAllData(contents)
                    recyclerGridAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onTextCleared(searchHistory: String?) {
    }

    override fun onSearchStarted() {

    }

    override fun onListModeChange(listMode: Int) {
        viewModel?.prevListMode?.postValue(listMode)
    }
    ///////////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    //////////////////////////////////////  OVERRIDDEN FROM OnSelectedFragment \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    override fun start(fragment: Fragment, position: Int) {
        Log.e("Start Fragment", "FragmentStart ${fragment.javaClass.simpleName}")
        //initViewModel()
        val listMode = mRequestIntoHostActivity?.getListMode()
        viewModel?.apply {

            if (hasFirstInstantiate.value == false) {
                prevListMode.value = listMode
                getAt(contentReqTypes, 1)
                fragmentIsRefreshing.value = true
                hasFirstInstantiate.value = true
            } else if (prevListMode.value != listMode) {
                prevListMode.value = listMode
            }
        }

    }
    ////////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\


    companion object {
        private const val EXTRA_CONTENT_REQUESTED_TYPES = "CONTENT_REQ_TYPE"
        private const val EXTRA_CONTENT_TYPES = "CONTENT_TYPE"

        @JvmStatic
        internal fun newInstance(
            typeOfContent: PublicConfig.ContentDisplayType,
            contentTypes: Parcelable
        ): FragmentContents =
            FragmentContents().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_CONTENT_REQUESTED_TYPES, contentTypes)
                    putParcelable(EXTRA_CONTENT_TYPES, typeOfContent)
                }
            }
    }
}