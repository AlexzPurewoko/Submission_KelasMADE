package id.apwdevs.moTvCatalogue.activities

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.DrawableRes
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import id.apwdevs.moTvCatalogue.R
import id.apwdevs.moTvCatalogue.fragment.MovieListFragment
import id.apwdevs.moTvCatalogue.fragment.TvViewListFragment
import id.apwdevs.moTvCatalogue.plugin.OnListModeChanged
import id.apwdevs.moTvCatalogue.plugin.OnSearchViewCallback
import kotlinx.android.synthetic.main.activity_main_user.*

class MainUserActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private var currentListMode = MODE_LIST
    private lateinit var mViewPager: ViewPager
    private lateinit var mSectionPagerAdapter: SectionPagerAdapter
    private lateinit var navView: BottomNavigationView
    private var mListFragment = mutableListOf<Fragment>()
    private var titleStr: String? = null
    private var currentPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_user)
        navView = nav_view
        mViewPager = frame_container.apply {
            mSectionPagerAdapter = SectionPagerAdapter(supportFragmentManager)
            adapter = mSectionPagerAdapter
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(i: Int, v: Float, i1: Int) {

                }

                override fun onPageSelected(i: Int) {
                    when (mListFragment[i]) {
                        is MovieListFragment -> {
                            navView.selectedItemId = R.id.nav_list_movie
                            titleStr = resources.getString(R.string.activity_user_mode_list_movie)
                        }
                        is TvViewListFragment -> {
                            navView.selectedItemId = R.id.nav_list_tv
                            titleStr = resources.getString(R.string.activity_user_mode_list_tv)
                        }
                    }
                    setTitle()
                }

                override fun onPageScrollStateChanged(i: Int) {

                }
            })
            setCurrentItem(0, true)
        }
        navView.setOnNavigationItemSelectedListener(this)
        titleStr = resources.getString(R.string.activity_user_mode_list_movie)
        setTitle()

        if (savedInstanceState == null) {
            mListFragment.add(MovieListFragment())
            mListFragment.add(TvViewListFragment())
            mSectionPagerAdapter.addAllFragment(mListFragment)
        } else {
            savedInstanceState.apply {
                titleStr = getString(EXTRA_TITLE_ACTIONBAR)
                mViewPager.setCurrentItem(getInt(EXTRA_POSITION), true)
                currentListMode = getInt(EXTRA_LIST_MODE)
                mListFragment = mutableListOf()
                var index = 0
                while (true) {
                    val fragment =
                        supportFragmentManager.getFragment(this, EXTRA_FRAGMENTS + "" + index++) ?: break
                    mListFragment.add(fragment)
                }
                mSectionPagerAdapter.addAllFragment(mListFragment)
                setTitle()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_user_menu, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        //var index : Int = 0
        for (index in 0 until menu.size()) {
            val resultMenu = menu.getItem(index) ?: break

            when (resultMenu.itemId) {
                R.id.menu_list_type -> setCurrentIconListMode(resultMenu)
                R.id.action_user_search ->
                    (resultMenu.actionView as SearchView).apply {
                        setSearchableInfo(searchManager.getSearchableInfo(componentName))
                        queryHint = context.getString(R.string.search_query_hint)
                        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String?): Boolean {
                                for (fragment in mListFragment) {
                                    if (fragment is OnSearchViewCallback)
                                        (fragment as OnSearchViewCallback).onQueryTextSubmitted(this@apply, query)
                                }
                                return false
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                for (fragment in mListFragment) {
                                    if (fragment is OnSearchViewCallback)
                                        (fragment as OnSearchViewCallback).onQueryTextChange(this@apply, newText)
                                }
                                return true
                            }

                        })
                        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                            override fun onViewDetachedFromWindow(v: View?) {
                                Snackbar.make(mViewPager, R.string.onclose_toast, Snackbar.LENGTH_INDEFINITE).apply {
                                    setAction(R.string.string_okay) {
                                        dismiss()
                                    }
                                }.show()
                                for (fragment in mListFragment) {
                                    if (fragment is OnSearchViewCallback)
                                        (fragment as OnSearchViewCallback).onSearchEnded(this@apply)
                                }
                            }

                            override fun onViewAttachedToWindow(v: View?) {

                            }

                        })
                    }
            }

        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_list_type -> {
                when (currentListMode) {
                    MODE_GRID -> currentListMode = MODE_LIST
                    MODE_LIST -> currentListMode = MODE_STAGERRED_LIST
                    MODE_STAGERRED_LIST -> currentListMode = MODE_GRID
                }
                for (fragment in mListFragment) {
                    if (fragment is OnListModeChanged) {
                        (fragment as OnListModeChanged).onListModeChanged(currentListMode)
                    }
                }
            }
            R.id.action_lang_system_setting -> {
                startActivity(
                    Intent(Settings.ACTION_LOCALE_SETTINGS)
                )
            }
        }
        setCurrentIconListMode(item)
        return super.onOptionsItemSelected(item)
    }

    private fun setCurrentIconListMode(item: MenuItem) {
        when (currentListMode) {
            MODE_GRID -> {
                setIconToolbar(R.id.menu_list_type, R.drawable.ic_view_list_white_24dp, item)
                item.title = getString(R.string.list_mode)
            }
            MODE_LIST -> {
                setIconToolbar(R.id.menu_list_type, R.drawable.ic_view_staggered_list_24dp, item)
                item.title = getString(R.string.staggerred_list_mode)
            }
            MODE_STAGERRED_LIST -> {
                setIconToolbar(R.id.menu_list_type, R.drawable.ic_view_card_grid_white_24dp, item)
                item.title = getString(R.string.grid_view)
            }
        }
    }

    private fun setIconToolbar(expectedId: Int, @DrawableRes iconResources: Int, item: MenuItem) {
        if (item.itemId == expectedId) {
            item.setIcon(iconResources)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var index = 0
        outState.apply {
            for (f in mListFragment) {
                supportFragmentManager.putFragment(this, EXTRA_FRAGMENTS + "" + index++, f)

            }
            putString(EXTRA_TITLE_ACTIONBAR, titleStr)
            putInt(EXTRA_POSITION, currentPos)
            putInt(EXTRA_LIST_MODE, currentListMode)

        }
    }

    private fun setTitle() {
        supportActionBar?.title = titleStr
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_list_movie -> {
                mViewPager.setCurrentItem(0, true)
                titleStr = resources.getString(R.string.activity_user_mode_list_movie)
                currentPos = 0
            }
            R.id.nav_list_tv -> {
                mViewPager.setCurrentItem(1, true)
                titleStr = resources.getString(R.string.activity_user_mode_list_tv)
                currentPos = 1
            }
        }
        setTitle()
        return true
    }

    companion object {
        private const val EXTRA_TITLE_ACTIONBAR = "TITLE_ACTBAR"
        private const val EXTRA_POSITION = "EXTRA_POSITION"
        private const val EXTRA_FRAGMENTS = "EXTRA_FRAGMENTS"
        const val MODE_GRID = 0x6ffa
        const val MODE_LIST = 0x5faa
        const val MODE_STAGERRED_LIST = 0xaf45
        private const val EXTRA_LIST_MODE = "LIST_MODE"
    }

    private inner class SectionPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        val mListFragment: MutableList<Fragment> = mutableListOf()

        internal fun addAllFragment(mListFragment: List<Fragment>) {
            this.mListFragment.addAll(mListFragment)
            notifyDataSetChanged()
        }

        override fun getItem(i: Int): Fragment {
            return mListFragment[i]
        }

        override fun getCount(): Int {
            return mListFragment.size
        }
    }
}