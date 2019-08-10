package id.apwdevs.app.favoritedisplayer.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import id.apwdevs.app.favoritedisplayer.R
import id.apwdevs.app.favoritedisplayer.adapter.ListAdapter
import id.apwdevs.app.favoritedisplayer.model.FavoriteEntity
import id.apwdevs.app.favoritedisplayer.plugin.ItemClickSupport
import id.apwdevs.app.favoritedisplayer.plugin.OnFragmentCallbacks
import id.apwdevs.app.favoritedisplayer.repository.MainListRepository
import id.apwdevs.app.favoritedisplayer.viewmodel.MainListViewModel


class FragmentContents : Fragment(), OnRequestRefresh {

    private lateinit var refreshPage: SwipeRefreshLayout
    private lateinit var recyclerContent: RecyclerView
    private lateinit var recyclerListAdapter: ListAdapter

    private lateinit var mViewModel: MainListViewModel
    private lateinit var mLayoutError: LinearLayout
    private lateinit var mCardButton: CardView

    var onCallbacks: OnFragmentCallbacks? = null
    @Deprecated("")
    private var onContentRequestAllRefresh: OnRequestRefresh? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(MainListViewModel::class.java)
        mViewModel.type = arguments?.getParcelable(EXTRA_CONTENT_TYPES)
        recyclerListAdapter = ListAdapter(
            requireActivity() as AppCompatActivity
        ) {
            if (it) {
                onCallbacks?.onFavoriteChanged()
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
        mLayoutError = view.findViewById(R.id.no_display_linear)
        mCardButton = view.findViewById(R.id.card_button)
        mCardButton.setOnClickListener {
            Toast.makeText(requireContext(), "Hellelo", Toast.LENGTH_SHORT).show()
            val intent = Intent(ACTION_LAUNCH_MAIN)
            requireActivity().sendBroadcast(intent)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mViewModel.mFavList.observe(this, Observer {
            setupPage(it)
        })

        mViewModel.hasLoading.observe(this, Observer {
            refreshPage.isRefreshing = it
            if (it) {
                mLayoutError.visibility = View.GONE
            }
        })
        // we have to obtain a value of ViewModel
        refreshPage.setOnRefreshListener {
            mViewModel.load()
        }
        mViewModel.load()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onCallbacks = context as OnFragmentCallbacks
        } catch (e: ClassCastException) {
            Log.e("FragmentContents", "You Must Implement GetFromHostActivity to continue")
        }
    }

    override fun onDetach() {
        super.onDetach()
        onCallbacks = null
    }

    fun reload() {
        mViewModel.load()
    }

    fun setupPage(page: List<FavoriteEntity>?) {
        page?.let {
            if (it.isEmpty()) {
                mLayoutError.visibility = View.VISIBLE
                recyclerContent.visibility = View.GONE
                return
            }
            // set the layoutManager and the adapter
            recyclerContent.apply {

                mLayoutError.visibility = View.GONE
                recyclerContent.visibility = View.VISIBLE
                // find the windowSize
                visibility = View.VISIBLE
                recyclerListAdapter.resetAllData(it)
                val wSize = Point()
                requireActivity().windowManager.defaultDisplay.getSize(wSize)
                ItemClickSupport.removeFrom(this)
                layoutManager = LinearLayoutManager(context)
                adapter = recyclerListAdapter
                recyclerListAdapter.notifyDataSetChanged()
                requestFocusFromTouch()

                ItemClickSupport.addTo(this)?.onItemClickListener = object : ItemClickSupport.OnItemClickListener {
                    override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                        onRecyclerItemClicked(recyclerView, position, v)
                    }

                }

            }
            return
        }
        mLayoutError.visibility = View.VISIBLE
    }

    private fun onRecyclerItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
        onCallbacks?.onItemClicked(this, recyclerView, position, v)
    }
    ////////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    override fun onForceRefresh(fragment: Fragment) {
        Log.d("SetsOnForce", "Sets to force load a fragment because due to database changes")
        mViewModel.load()
    }

    companion object {
        private const val EXTRA_CONTENT_TYPES = "CONTENT_TYPE"
        const val ACTION_LAUNCH_MAIN = "id.apwdevs.app.catalogue.LAUNCH_MAIN_ACTIVITY"
        const val ACTION_LAUNCH_DETAIL = "id.apwdevs.app.catalogue.LAUNCH_DETAIL_ACTIVITY"

        @JvmStatic
        internal fun newInstance(
            typeOfContent: MainListRepository.ContentDisplayType
        ): FragmentContents =
            FragmentContents().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_CONTENT_TYPES, typeOfContent)
                }
            }
    }
}

interface OnRequestRefresh {
    fun onForceRefresh(fragment: Fragment)
}
