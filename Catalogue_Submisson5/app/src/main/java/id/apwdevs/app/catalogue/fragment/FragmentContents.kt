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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.activities.GetFromHostActivity
import id.apwdevs.app.catalogue.adapter.GridAdapter
import id.apwdevs.app.catalogue.adapter.ListAdapter
import id.apwdevs.app.catalogue.entity.FavoriteResponse
import id.apwdevs.app.catalogue.model.ClassResponse
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onUserMain.MainDataItemResponse
import id.apwdevs.app.catalogue.plugin.*
import id.apwdevs.app.catalogue.plugin.callbacks.OnItemFragmentClickListener
import id.apwdevs.app.catalogue.plugin.callbacks.OnSelectedFragment
import id.apwdevs.app.catalogue.plugin.view.ErrorSectionAdapter
import id.apwdevs.app.catalogue.plugin.view.SearchToolbarCard
import id.apwdevs.app.catalogue.viewModel.MainListViewModel

class FragmentContents : Fragment(), SearchToolbarCard.OnSearchCallback, OnSelectedFragment, OnRequestRefresh {

    private lateinit var refreshPage: SwipeRefreshLayout
    private lateinit var errorLayout: ScrollView
    private lateinit var recyclerContent: RecyclerView

    internal lateinit var viewModel: MainListViewModel
    private lateinit var errorAdapter: ErrorSectionAdapter
    private lateinit var types: PublicContract.ContentDisplayType
    lateinit var contentReqTypes: Parcelable // order in MainListMovieModel and MainListTvModel or if this is fav pages its ordered into values of type
    private lateinit var recyclerListAdapter: ListAdapter<ResettableItem>
    private lateinit var recyclerGridAdapter: GridAdapter<ResettableItem>
    private var mRequestIntoHostActivity: GetFromHostActivity? = null
    var isRunWithoutLoadFirst: Boolean = false

    var onItemClickListener: OnItemFragmentClickListener? = null
    private var onContentRequestAllRefresh: OnRequestRefresh? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(
                MainListViewModel::class.java
            )
        viewModel.applyConfiguration()
        recyclerGridAdapter =
            GridAdapter(requireContext(), viewModel.cardItemBg, viewModel.colorredTextState, viewModel.backdropSize)
        recyclerListAdapter = ListAdapter(
            requireActivity() as AppCompatActivity,
            viewModel.cardItemBg,
            viewModel.colorredTextState,
            viewModel.backdropSize
        ) {
            viewModel.getAt(contentReqTypes, 1)
            onContentRequestAllRefresh?.onForceRefresh(this@FragmentContents)
        }

        arguments?.apply {
            types = getParcelable(EXTRA_CONTENT_TYPES)
            when (types) {
                PublicContract.ContentDisplayType.MOVIE -> contentReqTypes =
                    getParcelable<MainListViewModel.MovieTypeContract>(EXTRA_CONTENT_REQUESTED_TYPES)
                PublicContract.ContentDisplayType.TV_SHOWS -> contentReqTypes =
                    getParcelable<MainListViewModel.TvTypeContract>(EXTRA_CONTENT_REQUESTED_TYPES)
                PublicContract.ContentDisplayType.FAVORITES -> contentReqTypes =
                    getParcelable<PublicContract.ContentDisplayType>(EXTRA_CONTENT_REQUESTED_TYPES)
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
        recyclerContent = view.findViewById(R.id.fg_content_recycler)
        refreshPage = view.findViewById(R.id.swipe_content_refresh)
        errorLayout = view.findViewById(R.id.error_section)
        errorAdapter = ErrorSectionAdapter(errorLayout)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // we have to obtain a value of ViewModel
        initViewModel()
        refreshPage.setOnRefreshListener {
            viewModel.getAt(contentReqTypes, 1)
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mRequestIntoHostActivity = context as GetFromHostActivity
            onContentRequestAllRefresh = context as OnRequestRefresh
        } catch (e: ClassCastException) {
            Log.e("FragmentContents", "You Must Implement GetFromHostActivity to continue")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mRequestIntoHostActivity = null
        onContentRequestAllRefresh = null
    }

    fun forceLoadContent(content: MainDataItemResponse?) {
        viewModel.forceLoadIn(content)
    }

    private fun initViewModel() {
        viewModel.apply {

            if (hasFirstInstantiate.value == false)
                setup(types)
            objData?.observe(this@FragmentContents, Observer {
                setupRecycler(it, prevListMode.value ?: 0)
            })
            prevListMode.observe(this@FragmentContents, Observer {
                setupRecycler(objData?.value, it)
            })
            isLoading?.observe(this@FragmentContents, Observer {
                val prevCond = refreshPage.isRefreshing
                refreshPage.isRefreshing = it

                if (!it && prevCond) {
                    recyclerListAdapter.notifyDataSetChanged()
                    recyclerGridAdapter.notifyDataSetChanged()
                }
            })
            retError?.observe(this@FragmentContents, Observer {
                if (it == null) {
                    recyclerContent.visible()
                    errorAdapter.unDisplayError()
                } else {
                    recyclerContent.invisible()
                    errorAdapter.displayError(it)
                }
            })

        }
    }

    private fun setupRecycler(page: ClassResponse?, listMode: Int) {

        page?.let {
            // set the layoutManager and the adapter
            recyclerContent.apply {
                // find the windowSize
                val wSize = Point()
                requireActivity().windowManager.defaultDisplay.getSize(wSize)
                ItemClickSupport.removeFrom(this)
                when (listMode) {
                    PublicContract.RecyclerMode.MODE_LIST -> {
                        layoutManager = LinearLayoutManager(context)
                        adapter = recyclerListAdapter
                        resetRecyclerListData(it)
                        recyclerListAdapter.notifyDataSetChanged()
                    }
                    PublicContract.RecyclerMode.MODE_GRID -> {
                        layoutManager = GridLayoutManager(context, calculateMaxColumn(context, wSize))
                        adapter = recyclerGridAdapter
                        resetRecyclerGridData(it)
                        recyclerGridAdapter.notifyDataSetChanged()
                    }
                    PublicContract.RecyclerMode.MODE_STAGERRED_LIST -> {
                        layoutManager = StaggeredGridLayoutManager(
                            calculateMaxColumn(context, wSize),
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        )
                        adapter = recyclerGridAdapter
                        resetRecyclerGridData(it)
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


    private fun resetRecyclerListData(p: ClassResponse?) {
        p?.let {
            recyclerListAdapter.resetAllData(
                when (it) {
                    is MainDataItemResponse -> it.contents
                    is FavoriteResponse -> it.listAll
                    else -> null
                }
            )
        }
    }

    private fun resetRecyclerGridData(p: ClassResponse?) {
        p?.let {
            recyclerGridAdapter.resetAllData(
                when (it) {
                    is MainDataItemResponse -> it.contents
                    is FavoriteResponse -> it.listAll
                    else -> null
                }
            )
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////// OVERRIDDEN FROM OnSearchCallback \\\\\\\\\\\\\\\\\\\\\\\\\\\\

    override fun querySearch(view: View, query: CharSequence?, start: Int, before: Int, count: Int) {
        when (viewModel.prevListMode.value) {
            PublicContract.RecyclerMode.MODE_LIST -> {
                if (query?.isNotBlank() == true) {
                    resetRecyclerListData(viewModel.objData?.value)
                    recyclerListAdapter.filter.filter(query)
                }
            }
            PublicContract.RecyclerMode.MODE_GRID, PublicContract.RecyclerMode.MODE_STAGERRED_LIST -> {
                resetRecyclerGridData(viewModel.objData?.value)
                if (query?.isNotBlank() == true)
                    recyclerGridAdapter.filter.filter(query)
            }
        }
    }

    override fun onSubmit(query: String) {

    }

    override fun onSearchCancelled() {
        viewModel.let {
            when (it.prevListMode.value) {
                PublicContract.RecyclerMode.MODE_LIST -> {
                    resetRecyclerListData(it.objData?.value)
                    recyclerListAdapter.notifyDataSetChanged()
                }
                PublicContract.RecyclerMode.MODE_GRID, PublicContract.RecyclerMode.MODE_STAGERRED_LIST -> {
                    resetRecyclerGridData(it.objData?.value)
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
        viewModel.prevListMode.postValue(listMode)
    }
    ///////////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    //////////////////////////////////////  OVERRIDDEN FROM OnSelectedFragment \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    override fun start(fragment: Fragment, position: Int) {
        Log.e("Start Fragment", "FragmentStart ${fragment.javaClass.simpleName}")
        //initViewModel()
        val listMode = mRequestIntoHostActivity?.getListMode()
        viewModel.apply {

            when {
                hasFirstInstantiate.value == false -> {
                    prevListMode.value = listMode
                    if (!isRunWithoutLoadFirst)
                        getAt(contentReqTypes, 1)
                    isRunWithoutLoadFirst = false
                    hasFirstInstantiate.value = true
                }
                prevListMode.value != listMode -> prevListMode.value = listMode
                types == PublicContract.ContentDisplayType.FAVORITES || hasForceLoadContent.value == true -> {
                    getAt(contentReqTypes, 1)
                    hasForceLoadContent.value = false
                }
            }
        }

    }
    ////////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    override fun onForceRefresh(fragment: Fragment) {
        Log.d("SetsOnForce", "Sets to force load a fragment because due to database changes")
        viewModel.hasForceLoadContent.value = true
    }

    companion object {
        private const val EXTRA_CONTENT_REQUESTED_TYPES = "CONTENT_REQ_TYPE"
        private const val EXTRA_CONTENT_TYPES = "CONTENT_TYPE"

        @JvmStatic
        internal fun newInstance(
            typeOfContent: PublicContract.ContentDisplayType,
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

interface OnRequestRefresh {
    fun onForceRefresh(fragment: Fragment)
}
