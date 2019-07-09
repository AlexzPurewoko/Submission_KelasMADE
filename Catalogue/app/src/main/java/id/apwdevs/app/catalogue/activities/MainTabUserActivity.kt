package id.apwdevs.app.catalogue.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.androidnetworking.AndroidNetworking
import com.google.android.material.tabs.TabLayout
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.fragment.FragmentListCallback
import id.apwdevs.app.catalogue.fragment.FragmentListContainer
import id.apwdevs.app.catalogue.fragment.HolderPageAdapter
import id.apwdevs.app.catalogue.plugin.view.SearchToolbarCard
import kotlinx.android.synthetic.main.activity_main_tab_user.*
import kotlinx.android.synthetic.main.search_toolbar.*

class MainTabUserActivity : AppCompatActivity(), SearchToolbarCard.OnSearchCallback, FragmentListCallback {

    private lateinit var searchToolbarCard: SearchToolbarCard
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_tab_user)
        AndroidNetworking.initialize(applicationContext)
        view_pager.adapter = HolderPageAdapter(supportFragmentManager, this)
        view_pager.offscreenPageLimit = 2
        searchToolbarCard = SearchToolbarCard(this, toolbar_card, this)
        setupTabs()
        setupVPager()
    }

    private fun setupVPager() {
        view_pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
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

    fun setupTabs() {
        // list to be added
        val strTitle = arrayListOf(
            R.string.movie_title,
            R.string.tv_title
        )
        val strImgs = arrayListOf(
            R.drawable.ic_movie_black_24dp,
            R.drawable.ic_tv_black_24dp
        )
        for((idx, strId) in strTitle.withIndex()) {
            tabs.addTab(
                tabs.newTab().apply {
                    customView = LayoutInflater.from(this@MainTabUserActivity).inflate(R.layout.tab_custom, tabs, false).apply {
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
                if(fg is SearchToolbarCard.OnSearchCallback)
                    fg.querySearch(view, query, start, before, count)
        }
    }

    override fun onSubmit(query: String) {

        (view_pager.adapter as HolderPageAdapter?)?.apply {
            // broadcasts to all of fragment in this view pager
                val fg = this.getAt(view_pager.currentItem)
                if(fg is SearchToolbarCard.OnSearchCallback)
                    fg.onSubmit(query)

        }
    }
    override fun onSearchCancelled() {
        (view_pager.adapter as HolderPageAdapter?)?.apply {
                val fg = this.getAt(view_pager.currentItem)
                if(fg is SearchToolbarCard.OnSearchCallback)
                    fg.onSearchCancelled()
        }
    }

    override fun onTextCleared(searchHistory: String?) {

        (view_pager.adapter as HolderPageAdapter?)?.apply {
                val fg = this.getAt(view_pager.currentItem)
                if(fg is SearchToolbarCard.OnSearchCallback)
                    fg.onTextCleared(searchHistory)
        }
    }

    override fun onSearchStarted() {

        (view_pager.adapter as HolderPageAdapter?)?.apply {
                val fg = this.getAt(view_pager.currentItem)
                if(fg is SearchToolbarCard.OnSearchCallback)
                    fg.onSearchStarted()
        }
    }

    override fun onListModeChange(listMode: Int) {
        Toast.makeText(this, "onListModeChange", Toast.LENGTH_SHORT).show()
        (view_pager.adapter as HolderPageAdapter?)?.apply {
            // broadcasts to all of fragment in this view pager
            for(i in 0 until count){
                val fg = this.getAt(i)
                if(fg is SearchToolbarCard.OnSearchCallback)
                    fg.onListModeChange(listMode)
            }
        }
    }


    override fun onFragmentChange(newFragment: Fragment, fragmentType: FragmentListContainer.Type) {
        searchToolbarCard.forceSearchCancel()
    }

}