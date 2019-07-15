package id.apwdevs.app.catalogue.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.plugin.CoroutineContextProvider
import id.apwdevs.app.catalogue.plugin.OnItemFragmentClickListener
import id.apwdevs.app.catalogue.plugin.view.SearchToolbarCard
import id.apwdevs.app.catalogue.viewModel.MainListMovieViewModel
import id.apwdevs.app.catalogue.viewModel.MainListTvViewModel
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentListContainer : Fragment(), SearchToolbarCard.OnSearchCallback, OnItemFragmentClickListener {

    private lateinit var bottomNavigationView: BottomNavigationView
    //private lateinit var vpager: ViewPager
    private lateinit var frame: FrameLayout

    private lateinit var mFragments: MutableList<Fragment>
    private lateinit var type: Type
    private var selectedPosition = 1


    var onItemClickListener: OnItemFragmentClickListener? = null

    private val fragmentTag: String
        get() {
            when (type) {
                Type.MOVIES -> {
                    return "FragmentListContainerMovie"
                }
                Type.TV_SHOWS -> {
                    return "FragmentListContainerTvShows"
                }
            }
        }
    var fragmentListCb: FragmentListCallback? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        return LayoutInflater.from(requireContext()).inflate(
            when (type) {
                Type.MOVIES -> R.layout.fg_holder_movies
                Type.TV_SHOWS -> R.layout.fg_holder_tv
            }, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        frame = view.findViewById(R.id.fg_content_frame)
        bottomNavigationView = view.findViewById(R.id.fg_content_bottomnav)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = requireNotNull(arguments).getParcelable(EXTRA_TYPE)
        if (savedInstanceState == null) {
            when (type) {
                Type.TV_SHOWS -> {
                    mFragments = mutableListOf(
                        FragmentTvContent.newInstance(MainListTvViewModel.SupportedType.TV_AIRING_TODAY),
                        FragmentTvContent.newInstance(MainListTvViewModel.SupportedType.TV_OTA),
                        FragmentTvContent.newInstance(MainListTvViewModel.SupportedType.DISCOVER),
                        FragmentTvContent.newInstance(MainListTvViewModel.SupportedType.POPULAR),
                        FragmentTvContent.newInstance(MainListTvViewModel.SupportedType.TOP_RATED)
                    )
                }
                Type.MOVIES -> {
                    mFragments = mutableListOf(
                        FragmentMovieContent.newInstance(MainListMovieViewModel.SupportedType.NOW_PLAYING),
                        FragmentMovieContent.newInstance(MainListMovieViewModel.SupportedType.POPULAR),
                        FragmentMovieContent.newInstance(MainListMovieViewModel.SupportedType.DISCOVER),
                        FragmentMovieContent.newInstance(MainListMovieViewModel.SupportedType.TOP_RATED),
                        FragmentMovieContent.newInstance(MainListMovieViewModel.SupportedType.UPCOMING)
                    )
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
        for (mFragment in mFragments) {
            if (mFragment is FragmentTvContent)
                mFragment.onItemClickListener = this
            else if (mFragment is FragmentMovieContent)
                mFragment.onItemClickListener = this
        }
    }


    override fun onItemClicked(fg: Fragment, recyclerView: RecyclerView, position: Int, v: View) {
        onItemClickListener?.onItemClicked(fg, recyclerView, position, v)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        for ((idx, fg) in mFragments.withIndex()) {
            childFragmentManager.putFragment(outState, "Content$fragmentTag$idx", fg)
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val menuSize = bottomNavigationView.menu.size()
        val currItem =
            if (menuSize % 2 == 0) 0
            else (menuSize / 2)

        bottomNavigationView.setOnNavigationItemSelectedListener { itemSelectedMenu ->
            val menus = bottomNavigationView.menu
            for (menuPos in 0 until menus.size()) {
                if (menus[menuPos].itemId == itemSelectedMenu.itemId) {
                    setCurrentFragment(menuPos)
                    fragmentListCb?.onFragmentChange(mFragments[menuPos], type)
                    selectedPosition = menuPos
                    startPrepareFragment(menuPos)
                    break
                }
            }
            true
        }
        //vpager.adapter = ContentPageAdapter(childFragmentManager, mFragments)
        //bottomNavigationView.selectedItemId = bottomNavigationView.menu.getItem(currItem).itemId
    }

    private fun setCurrentFragment(position: Int) {
        fragmentManager?.beginTransaction()?.apply {
            replace(R.id.fg_content_frame, mFragments[position])
            addToBackStack("$fragmentTag$position")
            commit()
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
        val fg = mFragments[selectedPosition]
        if (fg is SearchToolbarCard.OnSearchCallback) {
            fg.querySearch(view, query, start, before, count)
        }
    }

    override fun onSubmit(query: String) {

    }

    override fun onSearchCancelled() {
        val fg = mFragments[selectedPosition]
        if (fg is SearchToolbarCard.OnSearchCallback) {
            fg.onSearchCancelled()
        }
    }

    override fun onTextCleared(searchHistory: String?) {
        onSearchCancelled()
    }

    override fun onSearchStarted() {

    }

    override fun onListModeChange(listMode: Int) {
        mFragments.forEach {
            if (it is SearchToolbarCard.OnSearchCallback) {
                it.onListModeChange(listMode)
            }
        }
    }


    @Parcelize
    enum class Type : Parcelable {
        TV_SHOWS,
        MOVIES
    }

    companion object {
        const val EXTRA_TYPE = "EXTRA_TYPE"
        @JvmStatic
        fun newInstance(type: Type): FragmentListContainer =
            FragmentListContainer().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_TYPE, type)
                }
            }
    }
}

internal class ContentPageAdapter(fragmentManager: FragmentManager, private val fragments: MutableList<Fragment>) :
    FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment = fragments[position]

    override fun getCount(): Int = fragments.size

}

internal class HolderPageAdapter(
    fragmentManager: FragmentManager,
    private val listFragment: List<Fragment>,
    private val fragmentCb: FragmentListCallback
) :
    FragmentStatePagerAdapter(fragmentManager), FragmentListCallback, OnSelectedFragment {

    override fun getItem(position: Int): Fragment = listFragment[position]

    override fun getCount(): Int = listFragment.size


    override fun finishUpdate(container: ViewGroup) {
        super.finishUpdate(container)
        listFragment.forEach {
            if (it is FragmentListContainer) {
                it.fragmentListCb = this@HolderPageAdapter
            }
        }
    }


    override fun start(fragment: Fragment, position: Int) {
    }

    fun getAt(position: Int): Fragment = getItem(position)

    override fun onFragmentChange(newFragment: Fragment, fragmentType: FragmentListContainer.Type) {
        fragmentCb.onFragmentChange(newFragment, fragmentType)
    }


}

interface FragmentListCallback {
    fun onFragmentChange(newFragment: Fragment, fragmentType: FragmentListContainer.Type)
}

interface OnSelectedFragment {
    fun start(fragment: Fragment, position: Int)
}