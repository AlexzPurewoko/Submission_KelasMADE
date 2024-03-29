package id.apwdevs.app.catalogue.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.androidnetworking.AndroidNetworking
import com.google.android.material.tabs.TabLayout
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.adapter.GridAdapter
import id.apwdevs.app.catalogue.adapter.ListAdapter
import id.apwdevs.app.catalogue.fragment.FragmentContents
import id.apwdevs.app.catalogue.fragment.FragmentListContainer
import id.apwdevs.app.catalogue.fragment.HolderPageAdapter
import id.apwdevs.app.catalogue.model.onUserMain.MovieAboutModel
import id.apwdevs.app.catalogue.model.onUserMain.TvAboutModel
import id.apwdevs.app.catalogue.plugin.PublicConfig
import id.apwdevs.app.catalogue.plugin.callbacks.FragmentListCallback
import id.apwdevs.app.catalogue.plugin.callbacks.OnItemFragmentClickListener
import id.apwdevs.app.catalogue.plugin.view.SearchToolbarCard
import kotlinx.android.synthetic.main.activity_main_tab_user.*
import kotlinx.android.synthetic.main.search_toolbar.*

class MainTabUserActivity : AppCompatActivity(), SearchToolbarCard.OnSearchCallback, FragmentListCallback,
    OnItemFragmentClickListener, GetFromHostActivity {

    private lateinit var searchToolbarCard: SearchToolbarCard
    private lateinit var listFragmentContainer: MutableList<Fragment>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_tab_user)
        AndroidNetworking.initialize(applicationContext)
        if (savedInstanceState == null) {
            listFragmentContainer = mutableListOf(
                FragmentListContainer.newInstance(PublicConfig.ContentDisplayType.MOVIE),
                FragmentListContainer.newInstance(PublicConfig.ContentDisplayType.TV_SHOWS)
            )
        } else {
            savedInstanceState.let {
                var idx = 0
                listFragmentContainer = mutableListOf()
                while (true) {
                    try {
                        val fg = supportFragmentManager.getFragment(it, "FgContainer${idx++}")
                            ?: break
                        listFragmentContainer.add(fg)
                    } catch (e: Exception) {
                        break
                    }
                }
            }
        }
        searchToolbarCard = SearchToolbarCard(this, toolbar_card, this)
        view_pager.adapter = HolderPageAdapter(supportFragmentManager, listFragmentContainer)
        view_pager.offscreenPageLimit = 2
        setupTabs()
        setupVPager()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // we wants to save container fragments
        for ((idx, fg) in listFragmentContainer.withIndex()) {
            try {
                supportFragmentManager.putFragment(outState, "FgContainer$idx", fg)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    override fun onPause() {
        searchToolbarCard.close()
        super.onPause()
    }

    override fun onDestroy() {
        searchToolbarCard.close()
        super.onDestroy()
    }

    override fun onItemClicked(fg: Fragment, recyclerView: RecyclerView, position: Int, v: View) {

        startActivity(
            Intent(this, DetailActivity::class.java).apply {
                val adapter = recyclerView.adapter
                if (adapter is ListAdapter<*>) {
                    val model = adapter.dataModel
                    if (model.size > 0) {
                        adapter.resetAllSpannables()
                        if (fg is FragmentContents) {
                            val type = (listFragmentContainer[view_pager.currentItem] as FragmentListContainer).type
                            val selected = model[position]
                            putExtras(Bundle().apply {
                                putParcelable(
                                    DetailActivity.EXTRA_DETAIL_TYPES,
                                    type
                                )
                                when (type) {
                                    PublicConfig.ContentDisplayType.TV_SHOWS -> {
                                        (selected as TvAboutModel).let {
                                            putExtra(DetailActivity.EXTRA_ID, it.idTv)
                                            putExtra(DetailActivity.EXTRA_CONTENT_DETAILS, it)
                                        }
                                    }
                                    PublicConfig.ContentDisplayType.MOVIE -> {
                                        (selected as MovieAboutModel).let {
                                            putExtra(DetailActivity.EXTRA_ID, it.id)
                                            putExtra(DetailActivity.EXTRA_CONTENT_DETAILS, it)
                                        }
                                    }
                                }

                            })
                        }

                    }
                } else if (adapter is GridAdapter<*>) {
                    val model = adapter.shortListModels
                    if (model.size > 0) {
                        adapter.resetAllSpannables()
                        if (fg is FragmentContents) {
                            val type = (listFragmentContainer[view_pager.currentItem] as FragmentListContainer).type
                            val selected = model[position]
                            putExtras(Bundle().apply {
                                putParcelable(
                                    DetailActivity.EXTRA_DETAIL_TYPES,
                                    type
                                )
                                when (type) {
                                    PublicConfig.ContentDisplayType.TV_SHOWS -> {
                                        (selected as TvAboutModel).let {
                                            putExtra(DetailActivity.EXTRA_ID, it.idTv)
                                            putExtra(DetailActivity.EXTRA_CONTENT_DETAILS, it)
                                        }
                                    }
                                    PublicConfig.ContentDisplayType.MOVIE -> {
                                        (selected as MovieAboutModel).let {
                                            putExtra(DetailActivity.EXTRA_ID, it.id)
                                            putExtra(DetailActivity.EXTRA_CONTENT_DETAILS, it)
                                        }
                                    }
                                }

                            })
                        }
                    }
                }
            }
        )


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
            }

        })
    }

    override fun querySearch(view: View, query: CharSequence?, start: Int, before: Int, count: Int) {
        // forward into next-child current fragment
        listFragmentContainer[view_pager.currentItem].apply {
            if (this is SearchToolbarCard.OnSearchCallback)
                querySearch(view, query, start, before, count)
        }
    }

    override fun onSubmit(query: String) {
        listFragmentContainer[view_pager.currentItem].apply {
            if (this is SearchToolbarCard.OnSearchCallback)
                onSubmit(query)
        }
    }

    override fun onSearchCancelled() {
        listFragmentContainer[view_pager.currentItem].apply {
            if (this is SearchToolbarCard.OnSearchCallback)
                onSearchCancelled()
        }
    }

    override fun onTextCleared(searchHistory: String?) {
        listFragmentContainer[view_pager.currentItem].apply {
            if (this is SearchToolbarCard.OnSearchCallback)
                onTextCleared(searchHistory)
        }
    }

    override fun onSearchStarted() {
        listFragmentContainer[view_pager.currentItem].apply {
            if (this is SearchToolbarCard.OnSearchCallback)
                onSearchStarted()
        }
    }

    override fun onListModeChange(listMode: Int) {
        listFragmentContainer[view_pager.currentItem].apply {
            if (this is SearchToolbarCard.OnSearchCallback)
                onListModeChange(listMode)
        }
        searchToolbarCard.forceSearchCancel()
    }

    override fun getListMode(): Int = searchToolbarCard.currentListMode ?: PublicConfig.RecyclerMode.MODE_LIST

    override fun onFragmentChange(newFragment: Fragment, fragmentType: PublicConfig.ContentDisplayType) {
        searchToolbarCard.forceSearchCancel()
    }
}

interface GetFromHostActivity {
    fun getListMode(): Int
}