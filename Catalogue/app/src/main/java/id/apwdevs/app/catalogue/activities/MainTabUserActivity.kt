package id.apwdevs.app.catalogue.activities

import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.androidnetworking.AndroidNetworking
import com.google.android.material.tabs.TabLayout
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.adapter.GridAdapter
import id.apwdevs.app.catalogue.adapter.ListAdapter
import id.apwdevs.app.catalogue.fragment.*
import id.apwdevs.app.catalogue.model.onUserMain.MovieAboutModel
import id.apwdevs.app.catalogue.model.onUserMain.TvAboutModel
import id.apwdevs.app.catalogue.plugin.OnItemFragmentClickListener
import id.apwdevs.app.catalogue.plugin.view.SearchToolbarCard
import kotlinx.android.synthetic.main.activity_main_tab_user.*
import kotlinx.android.synthetic.main.search_toolbar.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainTabUserActivity : AppCompatActivity(), SearchToolbarCard.OnSearchCallback, FragmentListCallback,
    OnItemFragmentClickListener {


    private lateinit var searchToolbarCard: SearchToolbarCard
    private lateinit var listFragmentContainer: MutableList<Fragment>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_tab_user)
        AndroidNetworking.initialize(applicationContext)
        searchToolbarCard = SearchToolbarCard(this, toolbar_card, this)
        if (savedInstanceState == null) {
            listFragmentContainer = mutableListOf(
                FragmentListContainer.newInstance(FragmentListContainer.Type.MOVIES),
                FragmentListContainer.newInstance(FragmentListContainer.Type.TV_SHOWS)
            )
        } else {
            savedInstanceState.let {
                var idx = 0
                listFragmentContainer = mutableListOf()
                while (true) {
                    try {
                        val fg = supportFragmentManager.getFragment(savedInstanceState, "FragmentListContainer${idx++}")
                            ?: break
                        listFragmentContainer.add(fg)
                    } catch (e: Exception) {
                        break
                    }
                }
            }
        }
        listFragmentContainer.forEach {

            if (it is FragmentListContainer)
                it.onItemClickListener = this
        }
        view_pager.adapter = HolderPageAdapter(supportFragmentManager, listFragmentContainer, this)
        view_pager.offscreenPageLimit = 2
        setupTabs()
        setupVPager()
        //removeExtraSpacing()
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        // we wants to save container fragments
        outState?.let {
            for ((idx, fg) in listFragmentContainer.withIndex()) {
                supportFragmentManager.putFragment(it, "FragmentListContainer$idx", fg)
            }
        }
    }

    override fun onItemClicked(fg: Fragment, recyclerView: RecyclerView, position: Int, v: View) {

        startActivity(
            Intent(this, DetailActivity::class.java).apply {
                val adapter = recyclerView.adapter
                if (adapter is ListAdapter<*>) {
                    val model = adapter.dataModel
                    if (model.size > 0) {
                        adapter.resetAllSpannables()
                        when (fg) {
                            is FragmentMovieContent -> {
                                val selected = model[position] as MovieAboutModel
                                putExtras(Bundle().apply {
                                    putExtra(DetailActivity.EXTRA_ID, selected.id)
                                    putParcelable(
                                        DetailActivity.EXTRA_DETAIL_TYPES,
                                        DetailActivity.ContentTypes.ITEM_MOVIE
                                    )
                                    putExtra(DetailActivity.EXTRA_CONTENT_DETAILS, selected)
                                })
                            }
                            is FragmentTvContent -> {
                                putExtras(Bundle().apply {

                                    val selected = model[position] as TvAboutModel
                                    putExtra(DetailActivity.EXTRA_ID, selected.idTv)
                                    putParcelable(
                                        DetailActivity.EXTRA_DETAIL_TYPES,
                                        DetailActivity.ContentTypes.ITEM_TV_SHOWS
                                    )
                                    putExtra(DetailActivity.EXTRA_CONTENT_DETAILS, selected)
                                })

                            }
                        }

                    }
                } else if (adapter is GridAdapter<*>) {
                    val model = adapter.shortListModels
                    if (model.size > 0) {
                        adapter.resetAllSpannables()
                        when (fg) {
                            is FragmentMovieContent -> {
                                val selected = model[position] as MovieAboutModel
                                putExtras(Bundle().apply {
                                    putExtra(DetailActivity.EXTRA_ID, selected.id)
                                    putParcelable(
                                        DetailActivity.EXTRA_DETAIL_TYPES,
                                        DetailActivity.ContentTypes.ITEM_MOVIE
                                    )
                                    putExtra(DetailActivity.EXTRA_CONTENT_DETAILS, selected)
                                })
                            }
                            is FragmentTvContent -> {
                                putExtras(Bundle().apply {

                                    val selected = model[position] as TvAboutModel
                                    putExtra(DetailActivity.EXTRA_ID, selected.idTv)
                                    putParcelable(
                                        DetailActivity.EXTRA_DETAIL_TYPES,
                                        DetailActivity.ContentTypes.ITEM_TV_SHOWS
                                    )
                                    putExtra(DetailActivity.EXTRA_CONTENT_DETAILS, selected)
                                })

                            }
                        }

                    }
                }
            }
        )


    }

    private fun removeExtraSpacing() {
        val wSize = Point()
        windowManager.defaultDisplay.getSize(wSize)

        GlobalScope.launch {
            var currHeight = 0
            var appBarHeight = 0
            view_pager.post {
                currHeight = view_pager.height
            }
            main_tab_appbar.post {
                appBarHeight = main_tab_appbar.height
            }

            while (currHeight == 0 || appBarHeight == 0) delay(200)
            val result = currHeight - appBarHeight
            view_pager.post {
                view_pager.layoutParams = (view_pager.layoutParams as CoordinatorLayout.LayoutParams).apply {
                    setMargins(0, 0, 0, appBarHeight)
                    //height = result
                }
            }
        }
    }

    private fun setupVPager() {
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                tabs.setScrollPosition(position, positionOffset, false)
            }

            override fun onPageSelected(position: Int) {
                tabs.getTabAt(position)?.select()
            }

        })
    }

    private fun setupTabs() {
        // list to be added
        val strTitle = arrayListOf(
            R.string.movie_title,
            R.string.tv_title
        )
        val strImgs = arrayListOf(
            R.drawable.ic_movie_24dp,
            R.drawable.ic_tv_24dp
        )
        for ((idx, strId) in strTitle.withIndex()) {
            tabs.addTab(
                tabs.newTab().apply {
                    customView =
                        LayoutInflater.from(this@MainTabUserActivity).inflate(R.layout.tab_custom, tabs, false).apply {
                            findViewById<TextView>(R.id.tab_title).setText(strId)
                            findViewById<ImageView>(R.id.tab_icon).setImageResource(strImgs[idx])
                        }
                }
            )
        }
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                view_pager.setCurrentItem(p0?.position ?: 0, true)
                main_tab_appbar.setExpanded(true, true)
            }

        })
    }

    override fun querySearch(view: View, query: CharSequence?, start: Int, before: Int, count: Int) {
        // forward into next-child current fragment
        (view_pager.adapter as HolderPageAdapter?)?.apply {
            // broadcasts to all of fragment in this view pager
            val fg = this.getAt(view_pager.currentItem)
            if (fg is SearchToolbarCard.OnSearchCallback && fg.isResumed)
                fg.querySearch(view, query, start, before, count)
        }
    }

    override fun onSubmit(query: String) {

        (view_pager.adapter as HolderPageAdapter?)?.apply {
            // broadcasts to all of fragment in this view pager
            val fg = this.getAt(view_pager.currentItem)
            if (fg is SearchToolbarCard.OnSearchCallback && fg.isResumed)
                fg.onSubmit(query)

        }
    }

    override fun onSearchCancelled() {
        (view_pager.adapter as HolderPageAdapter?)?.apply {
            val fg = this.getAt(view_pager.currentItem)
            if (fg is SearchToolbarCard.OnSearchCallback && fg.isResumed)
                fg.onSearchCancelled()
        }
    }

    override fun onTextCleared(searchHistory: String?) {

        (view_pager.adapter as HolderPageAdapter?)?.apply {
            val fg = this.getAt(view_pager.currentItem)
            if (fg is SearchToolbarCard.OnSearchCallback && fg.isResumed)
                fg.onTextCleared(searchHistory)
        }
    }

    override fun onSearchStarted() {

        (view_pager.adapter as HolderPageAdapter?)?.apply {
            val fg = this.getAt(view_pager.currentItem)
            if (fg is SearchToolbarCard.OnSearchCallback && fg.isResumed)
                fg.onSearchStarted()
        }
    }

    override fun onListModeChange(listMode: Int) {
        Toast.makeText(this, "onListModeChange", Toast.LENGTH_SHORT).show()
        (view_pager.adapter as HolderPageAdapter?)?.apply {
            // broadcasts to all of fragment in this view pager
            for (i in 0 until count) {
                val fg = this.getAt(i)
                if (fg is SearchToolbarCard.OnSearchCallback)
                    fg.onListModeChange(listMode)
            }
        }
    }


    override fun onFragmentChange(newFragment: Fragment, fragmentType: FragmentListContainer.Type) {
        searchToolbarCard.forceSearchCancel()
    }

}