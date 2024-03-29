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
import id.apwdevs.app.catalogue.plugin.CoroutineContextProvider
import id.apwdevs.app.catalogue.plugin.PublicConfig
import id.apwdevs.app.catalogue.plugin.callbacks.FragmentListCallback
import id.apwdevs.app.catalogue.plugin.callbacks.OnItemFragmentClickListener
import id.apwdevs.app.catalogue.plugin.callbacks.OnSelectedFragment
import id.apwdevs.app.catalogue.plugin.view.SearchToolbarCard
import id.apwdevs.app.catalogue.viewModel.MainListMovieViewModel
import id.apwdevs.app.catalogue.viewModel.MainListTvViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentListContainer : Fragment(), SearchToolbarCard.OnSearchCallback,
    OnItemFragmentClickListener {


    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var vpager: ViewPager

    private lateinit var mFragments: MutableList<Fragment>
    lateinit var type: PublicConfig.ContentDisplayType

    private val fragmentTag: String
        get() {
            return when (type) {
                PublicConfig.ContentDisplayType.MOVIE -> {
                    "FragmentListContainerMovie"
                }
                PublicConfig.ContentDisplayType.TV_SHOWS -> {
                    "FragmentListContainerTvShows"
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
                PublicConfig.ContentDisplayType.MOVIE -> R.layout.fg_holder_movies
                PublicConfig.ContentDisplayType.TV_SHOWS -> R.layout.fg_holder_tv
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
                MainListTvViewModel.SupportedType.TV_AIRING_TODAY,
                MainListTvViewModel.SupportedType.TV_OTA,
                MainListTvViewModel.SupportedType.DISCOVER,
                MainListTvViewModel.SupportedType.POPULAR,
                MainListTvViewModel.SupportedType.TOP_RATED
            )
            val movieModeSupport = mutableListOf(
                MainListMovieViewModel.SupportedType.NOW_PLAYING,
                MainListMovieViewModel.SupportedType.POPULAR,
                MainListMovieViewModel.SupportedType.DISCOVER,
                MainListMovieViewModel.SupportedType.TOP_RATED,
                MainListMovieViewModel.SupportedType.UPCOMING
            )
            mFragments = mutableListOf()
            when (type) {
                PublicConfig.ContentDisplayType.TV_SHOWS -> {
                    tvModeSupport.forEach {
                        mFragments.add(FragmentContents.newInstance(type, it))
                    }

                }
                PublicConfig.ContentDisplayType.MOVIE -> {
                    movieModeSupport.forEach {
                        mFragments.add(FragmentContents.newInstance(type, it))
                    }
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
            if (it is FragmentContents)
                it.onItemClickListener = this
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


    companion object {
        const val EXTRA_TYPE = "EXTRA_TYPE"
        @JvmStatic
        fun newInstance(type: PublicConfig.ContentDisplayType): FragmentListContainer =
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
