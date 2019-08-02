package id.apwdevs.app.catalogue.activities

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
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
import id.apwdevs.app.catalogue.entity.FavoriteEntity
import id.apwdevs.app.catalogue.fragment.FragmentContents
import id.apwdevs.app.catalogue.fragment.FragmentListContainer
import id.apwdevs.app.catalogue.fragment.HolderPageAdapter
import id.apwdevs.app.catalogue.fragment.OnRequestRefresh
import id.apwdevs.app.catalogue.model.onUserMain.MovieAboutModel
import id.apwdevs.app.catalogue.model.onUserMain.TvAboutModel
import id.apwdevs.app.catalogue.plugin.ApplyLanguage
import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.plugin.callbacks.FragmentListCallback
import id.apwdevs.app.catalogue.plugin.callbacks.OnItemFragmentClickListener
import id.apwdevs.app.catalogue.plugin.view.SearchToolbarCard
import kotlinx.android.synthetic.main.activity_main_tab_user.*
import kotlinx.android.synthetic.main.search_toolbar.*
import java.util.*

class MainTabUserActivity : AppCompatActivity(), SearchToolbarCard.OnSearchCallback, FragmentListCallback,
    OnItemFragmentClickListener, GetFromHostActivity, OnRequestRefresh {

    companion object {
        const val LISTEN_FOR_LAYOUT_CHANGES = 0x55f
        const val LAYOUT_REQUEST_UPDATE = 0x4f
        const val NO_REQUEST = 0x5a
    }
    private lateinit var searchToolbarCard: SearchToolbarCard
    private lateinit var listFragmentContainer: MutableList<Fragment>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_tab_user)
        AndroidNetworking.initialize(applicationContext)
        if (savedInstanceState == null) {
            listFragmentContainer = mutableListOf(
                FragmentListContainer.newInstance(PublicContract.ContentDisplayType.MOVIE),
                FragmentListContainer.newInstance(PublicContract.ContentDisplayType.TV_SHOWS),
                FragmentListContainer.newInstance(PublicContract.ContentDisplayType.FAVORITES)
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
        view_pager.offscreenPageLimit = 3
        setupTabs()
        setupVPager()
    }

    fun applyConfiguration(res: Resources) {
        getSharedPreferences(PublicContract.SHARED_PREF_GLOBAL_NAME, Context.MODE_PRIVATE).apply {
            when (getString("app_languages", "system")) {
                "force_en" -> changeLanguage(res, Locale("en"))
                "force_in" -> changeLanguage(res, Locale("in"))
            }
        }
    }

    @Suppress("DEPRECATION")
    fun changeLanguage(res: Resources, locale: Locale) {
        res.configuration.setLocale(locale)
        val conf = res.configuration
        conf.setLocale(locale)
        res.updateConfiguration(conf, res.displayMetrics)
    }

    override fun attachBaseContext(newBase: Context?) {
        newBase?.let {
            var newCtx: Context? = null
            it.getSharedPreferences(PublicContract.SHARED_PREF_GLOBAL_NAME, Context.MODE_PRIVATE).apply {
                when (getString("app_languages", "system")) {
                    "force_en" -> newCtx = ApplyLanguage.wrap(it, Locale("en"))
                    "force_in" -> newCtx = ApplyLanguage.wrap(it, Locale("in"))
                    else -> {
                        super.attachBaseContext(newBase)
                        return
                    }
                }
                super.attachBaseContext(newCtx)
            }
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LISTEN_FOR_LAYOUT_CHANGES && resultCode == LAYOUT_REQUEST_UPDATE) {
            listFragmentContainer.forEach {
                if (it is OnRequestRefresh)
                    it.onForceRefresh(Fragment())
            }
        }
    }

    override fun onItemClicked(fg: Fragment, recyclerView: RecyclerView, position: Int, v: View) {

        startActivityForResult(
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
                                    PublicContract.ContentDisplayType.TV_SHOWS -> {
                                        (selected as TvAboutModel).let {
                                            putExtra(DetailActivity.EXTRA_ID, it.idTv)
                                            putExtra(DetailActivity.EXTRA_CONTENT_DETAILS, it)
                                        }
                                    }
                                    PublicContract.ContentDisplayType.MOVIE -> {
                                        (selected as MovieAboutModel).let {
                                            putExtra(DetailActivity.EXTRA_ID, it.id)
                                            putExtra(DetailActivity.EXTRA_CONTENT_DETAILS, it)
                                        }
                                    }
                                    PublicContract.ContentDisplayType.FAVORITES -> {
                                        (selected as FavoriteEntity).let {
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
                                    PublicContract.ContentDisplayType.TV_SHOWS -> {
                                        (selected as TvAboutModel).let {
                                            putExtra(DetailActivity.EXTRA_ID, it.idTv)
                                            putExtra(DetailActivity.EXTRA_CONTENT_DETAILS, it)
                                        }
                                    }
                                    PublicContract.ContentDisplayType.MOVIE -> {
                                        (selected as MovieAboutModel).let {
                                            putExtra(DetailActivity.EXTRA_ID, it.id)
                                            putExtra(DetailActivity.EXTRA_CONTENT_DETAILS, it)
                                        }
                                    }
                                    PublicContract.ContentDisplayType.FAVORITES -> {
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
            },
            LISTEN_FOR_LAYOUT_CHANGES
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
            R.string.tv_title,
            R.string.favorite_title
        )
        val strImgs = arrayListOf(
            R.drawable.ic_movie_24dp,
            R.drawable.ic_tv_24dp,
            R.drawable.ic_favorite_activated_24dp
        )
        for ((idx, strId) in strTitle.withIndex()) {
            tabs.addTab(
                tabs.newTab().apply {
                    customView =
                        LayoutInflater.from(this@MainTabUserActivity).inflate(R.layout.tab_custom, tabs, false).apply {
                            findViewById<TextView>(R.id.tab_title).setText(strId)
                            findViewById<ImageView>(R.id.tab_icon).apply {
                                imageTintList = ColorStateList.valueOf(Color.WHITE)
                                setImageResource(strImgs[idx])
                            }
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

    override fun getListMode(): Int = searchToolbarCard.currentListMode ?: PublicContract.RecyclerMode.MODE_LIST

    override fun onFragmentChange(newFragment: Fragment, fragmentType: PublicContract.ContentDisplayType) {
        searchToolbarCard.forceSearchCancel()
    }


    override fun onForceRefresh(fragment: Fragment) {
        // make a broadcast into all fragments
        listFragmentContainer.forEach {
            if (it is OnRequestRefresh)
                it.onForceRefresh(fragment)
        }
    }
}

interface GetFromHostActivity {
    fun getListMode(): Int
}