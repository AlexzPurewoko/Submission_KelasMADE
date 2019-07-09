package id.apwdevs.app.catalogue.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.plugin.view.SearchToolbarCard
import id.apwdevs.app.catalogue.viewModel.MainListMovieViewModel
import id.apwdevs.app.catalogue.viewModel.MainListTvViewModel
import kotlinx.android.parcel.Parcelize

class FragmentListContainer : Fragment(), SearchToolbarCard.OnSearchCallback{

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var vpager : ViewPager

    private lateinit var mFragments : MutableList<Fragment>
    private lateinit var type: Type
    var fragmentListCb : FragmentListCallback? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        type = requireNotNull(arguments).getParcelable(EXTRA_TYPE)
        return LayoutInflater.from(requireContext()).inflate(
            when(type) {
                Type.MOVIES -> R.layout.fg_holder_movies
                Type.TV_SHOWS -> R.layout.fg_holder_tv
            }, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vpager = view.findViewById(R.id.fg_content_pager)
        bottomNavigationView = view.findViewById(R.id.fg_content_bottomnav)
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val menuSize = bottomNavigationView.menu.size()
        val currItem =
            if(menuSize%2 == 0) 0
            else (menuSize/2)
        vpager.offscreenPageLimit = 2
        vpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                bottomNavigationView.selectedItemId = bottomNavigationView.menu.getItem(position).itemId
                fragmentListCb?.onFragmentChange(mFragments[position], type)

            }

        })
        bottomNavigationView.setOnNavigationItemSelectedListener { itemSelectedMenu ->
            val menus = bottomNavigationView.menu
            for(menuPos in 0 until menus.size()){
                if(menus[menuPos].itemId == itemSelectedMenu.itemId){
                    vpager.setCurrentItem(menuPos, true)
                    fragmentListCb?.onFragmentChange(mFragments[menuPos], type)
                    break
                }
            }
            true
        }
        when(type){
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
        vpager.adapter = ContentPageAdapter(childFragmentManager, mFragments)
        bottomNavigationView.selectedItemId = bottomNavigationView.menu.getItem(currItem).itemId
        vpager.setCurrentItem(currItem, true)
    }


    override fun querySearch(view: View, query: CharSequence?, start: Int, before: Int, count: Int) {
        val fg = mFragments[vpager.currentItem]
        if(fg is SearchToolbarCard.OnSearchCallback){
            fg.querySearch(view, query, start, before, count)
        }
    }

    override fun onSubmit(query: String) {

    }
    override fun onSearchCancelled() {
        val fg = mFragments[vpager.currentItem]
        if(fg is SearchToolbarCard.OnSearchCallback){
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
            if(it is SearchToolbarCard.OnSearchCallback){
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
        fun newInstance(type: Type) : FragmentListContainer =
            FragmentListContainer().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_TYPE, type)
                }
            }
    }
}

internal class ContentPageAdapter(fragmentManager: FragmentManager, private val fragments: MutableList<Fragment>) : FragmentStatePagerAdapter(fragmentManager){

    override fun getItem(position: Int): Fragment = fragments[position]

    override fun getCount(): Int = fragments.size

}

internal class HolderPageAdapter(fragmentManager: FragmentManager, private val fragmentCb : FragmentListCallback) : FragmentStatePagerAdapter(fragmentManager), FragmentListCallback {

    private val listFragment = arrayListOf<Fragment>(
        FragmentListContainer.newInstance(FragmentListContainer.Type.MOVIES),
        FragmentListContainer.newInstance(FragmentListContainer.Type.TV_SHOWS)
    )

    override fun getItem(position: Int): Fragment = listFragment[position]

    override fun getCount(): Int = listFragment.size

    override fun finishUpdate(container: ViewGroup) {
        super.finishUpdate(container)
        listFragment.forEach{
            if(it is FragmentListContainer){
                it.fragmentListCb = this@HolderPageAdapter
            }
        }
    }

    fun getAt(position: Int): Fragment = getItem(position)

    override fun onFragmentChange(newFragment: Fragment, fragmentType: FragmentListContainer.Type) {
        fragmentCb.onFragmentChange(newFragment, fragmentType)
    }


}

interface FragmentListCallback {
    fun onFragmentChange(newFragment: Fragment, fragmentType: FragmentListContainer.Type)
}