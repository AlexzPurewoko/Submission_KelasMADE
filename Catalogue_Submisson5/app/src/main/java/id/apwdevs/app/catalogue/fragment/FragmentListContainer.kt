package id.apwdevs.app.catalogue.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.model.onUserMain.MainDataItemResponse
import id.apwdevs.app.catalogue.plugin.CoroutineContextProvider
import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.plugin.callbacks.FragmentListCallback
import id.apwdevs.app.catalogue.plugin.callbacks.OnItemFragmentClickListener
import id.apwdevs.app.catalogue.plugin.callbacks.OnSelectedFragment
import id.apwdevs.app.catalogue.plugin.view.SearchToolbarCard
import id.apwdevs.app.catalogue.viewModel.MainListViewModel.MovieTypeContract
import id.apwdevs.app.catalogue.viewModel.MainListViewModel.TvTypeContract
import id.apwdevs.app.catalogue.workers.ReleaseTodayReminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentListContainer : Fragment(), SearchToolbarCard.OnSearchCallback,
    OnItemFragmentClickListener, OnRequestRefresh {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var vpager: ViewPager

    private lateinit var mFragments: MutableList<Fragment>
    lateinit var type: PublicContract.ContentDisplayType

    private val fragmentTag: String
        get() {
            return when (type) {
                PublicContract.ContentDisplayType.MOVIE -> {
                    "FragmentListContainerMovie"
                }
                PublicContract.ContentDisplayType.TV_SHOWS -> {
                    "FragmentListContainerTvShows"
                }
                PublicContract.ContentDisplayType.FAVORITES -> {
                    "FragmentListContainerFavorites"
                }
            }
        }
    var fragmentListCb: FragmentListCallback? = null
    private var onItemClickListener: OnItemFragmentClickListener? = null
    private var currItem: Int = 0

    @Volatile
    private var dummyLockUntilFinish: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutInflater.from(requireContext()).inflate(
            when (type) {
                PublicContract.ContentDisplayType.MOVIE -> R.layout.fg_holder_movies
                PublicContract.ContentDisplayType.TV_SHOWS -> R.layout.fg_holder_tv
                PublicContract.ContentDisplayType.FAVORITES -> R.layout.fg_holder_favorites
            }, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vpager = view.findViewById(R.id.fg_content_pager)
        bottomNavigationView = view.findViewById(R.id.fg_content_bottomnav)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = requireNotNull(arguments).getParcelable(EXTRA_TYPE)
        if (savedInstanceState == null) {
            val tvModeSupport = mutableListOf(
                TvTypeContract.TV_AIRING_TODAY,
                TvTypeContract.TV_OTA,
                TvTypeContract.DISCOVER,
                TvTypeContract.POPULAR,
                TvTypeContract.TOP_RATED
            )
            val movieModeSupport = mutableListOf(
                MovieTypeContract.NOW_PLAYING,
                MovieTypeContract.POPULAR,
                MovieTypeContract.DISCOVER,
                MovieTypeContract.TOP_RATED,
                MovieTypeContract.UPCOMING
            )
            val favModeSupport = mutableListOf(
                PublicContract.ContentDisplayType.MOVIE,
                PublicContract.ContentDisplayType.TV_SHOWS

            )
            mFragments = mutableListOf()
            when (type) {
                PublicContract.ContentDisplayType.TV_SHOWS -> {
                    tvModeSupport.forEach {
                        mFragments.add(FragmentContents.newInstance(type, it))
                    }

                }
                PublicContract.ContentDisplayType.MOVIE -> {
                    movieModeSupport.forEach {
                        mFragments.add(FragmentContents.newInstance(type, it))
                    }
                }
                PublicContract.ContentDisplayType.FAVORITES ->
                    favModeSupport.forEach {
                        mFragments.add(FragmentContents.newInstance(type, it))
                    }

            }
        } else {
            mFragments = mutableListOf()
            var idx = 0
            while (true) {
                try {
                    val fg =
                        childFragmentManager.getFragment(savedInstanceState, "Content$fragmentTag${idx++}") ?: break
                    mFragments.add(fg)
                } catch (e: Exception) {
                    break
                }
            }
        }
        mFragments.forEach {
            if (it is FragmentContents) {
                it.onItemClickListener = this
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(EXTRA_CURRENT_ITEM_POSITION, currItem)
        for ((idx, fg) in mFragments.withIndex()) {
            childFragmentManager.putFragment(outState, "Content$fragmentTag$idx", fg)
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val menuSize = bottomNavigationView.menu.size()
        vpager.offscreenPageLimit = menuSize
        savedInstanceState?.let {
            currItem = it.getInt(EXTRA_CURRENT_ITEM_POSITION)
        }

        vpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (dummyLockUntilFinish) {
                    dummyLockUntilFinish = false
                    bottomNavigationView.selectedItemId = bottomNavigationView.menu.getItem(position).itemId
                    currItem = position
                    fragmentListCb?.onFragmentChange(mFragments[position], type)
                    startPrepareFragment(position)
                    dummyLockUntilFinish = true
                }
            }

        })
        bottomNavigationView.setOnNavigationItemSelectedListener { itemSelectedMenu ->
            val menus = bottomNavigationView.menu
            for (menuPos in 0 until menus.size()) {
                if (menus[menuPos].itemId == itemSelectedMenu.itemId && dummyLockUntilFinish) {
                    dummyLockUntilFinish = false
                    vpager.setCurrentItem(menuPos, true)
                    currItem = menuPos
                    fragmentListCb?.onFragmentChange(mFragments[menuPos], type)
                    startPrepareFragment(menuPos)
                    dummyLockUntilFinish = true
                    break
                }
            }
            true
        }
        vpager.adapter = ContentPageAdapter(childFragmentManager, mFragments)
    }

    override fun onResume() {
        super.onResume()
        dummyLockUntilFinish = true
        vpager.setCurrentItem(currItem, true)
        bottomNavigationView.selectedItemId = bottomNavigationView.menu.getItem(currItem).itemId

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onItemClickListener = context as OnItemFragmentClickListener
            fragmentListCb = context as FragmentListCallback
        } catch (e: ClassCastException) {
            Log.e("onAttach()", "You must implement OnItemFragmentClickListener and FragmentListCallback in your class")
        }
    }

    override fun onDetach() {
        super.onDetach()
        onItemClickListener = null
    }

    fun forceStartFragmentContent(
        from: Int,
        type: PublicContract.ContentDisplayType,
        contentData: MainDataItemResponse
    ) {

        if (from == ReleaseTodayReminder.FROM_REMINDER) {
            loop@ for ((idx, fg) in mFragments.withIndex()) {
                when (type) {
                    PublicContract.ContentDisplayType.MOVIE ->
                        if (fg is FragmentContents && fg.contentReqTypes == MovieTypeContract.DISCOVER) {
                            forceStart(idx, fg, contentData)
                            break@loop
                        }
                    PublicContract.ContentDisplayType.TV_SHOWS ->
                        if (fg is FragmentContents && fg.contentReqTypes == TvTypeContract.DISCOVER) {
                            forceStart(idx, fg, contentData)
                            break@loop
                        }
                    else -> break@loop
                }
            }

        }
    }

    private fun forceStart(idx: Int, fg: FragmentContents, contentData: MainDataItemResponse) {
        GlobalScope.launch(Dispatchers.Main) {
            vpager.currentItem = idx
            fg.isRunWithoutLoadFirst = true
            while (!fg.isVisible) delay(400)
            fg.forceLoadContent(contentData)
        }
    }

    private fun startPrepareFragment(position: Int) {
        val fg = mFragments[position]
        if (fg is OnSelectedFragment) {
            // we have to wait its fragment until done for creating its instance
            GlobalScope.launch(CoroutineContextProvider().main) {
                while (!fg.isAdded)
                    delay(500)
                Handler(Looper.getMainLooper()).post { fg.start(fg, position) }
            }
        }
    }


    override fun querySearch(view: View, query: CharSequence?, start: Int, before: Int, count: Int) {
        mFragments[vpager.currentItem].apply {
            if (this is SearchToolbarCard.OnSearchCallback)
                querySearch(view, query, start, before, count)
        }
    }

    override fun onItemClicked(fg: Fragment, recyclerView: RecyclerView, position: Int, v: View) {
        onItemClickListener?.onItemClicked(fg, recyclerView, position, v)
    }

    override fun onSubmit(query: String) {

    }

    override fun onSearchCancelled() {
        if (dummyLockUntilFinish) {
            mFragments[vpager.currentItem].apply {
                if (this is SearchToolbarCard.OnSearchCallback)
                    onSearchCancelled()
            }
        }
    }

    override fun onTextCleared(searchHistory: String?) {
        onSearchCancelled()
    }

    override fun onSearchStarted() {

    }

    override fun onListModeChange(listMode: Int) {
        mFragments[vpager.currentItem].apply {
            if (this is SearchToolbarCard.OnSearchCallback)
                onListModeChange(listMode)
        }
    }

    override fun onForceRefresh(fragment: Fragment) {
        mFragments.forEach {
            if (it is OnRequestRefresh)
                it.onForceRefresh(fragment)
        }
    }


    companion object {
        const val EXTRA_TYPE = "EXTRA_TYPE"
        @JvmStatic
        fun newInstance(type: PublicContract.ContentDisplayType): FragmentListContainer =
            FragmentListContainer().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_TYPE, type)
                }
            }

        private const val EXTRA_CURRENT_ITEM_POSITION = "CURRENT_POS"
    }
}

internal class ContentPageAdapter(fragmentManager: FragmentManager, private val fragments: MutableList<Fragment>) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment = fragments[position]

    override fun getCount(): Int = fragments.size

}

internal class HolderPageAdapter(
    fragmentManager: FragmentManager,
    private val listFragment: List<Fragment>
) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment = listFragment[position]

    override fun getCount(): Int = listFragment.size


}
