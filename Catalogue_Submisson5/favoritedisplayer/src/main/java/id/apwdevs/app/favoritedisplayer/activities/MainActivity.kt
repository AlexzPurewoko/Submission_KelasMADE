package id.apwdevs.app.favoritedisplayer.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.database.ContentObserver
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import id.apwdevs.app.favoritedisplayer.R
import id.apwdevs.app.favoritedisplayer.adapter.ListAdapter
import id.apwdevs.app.favoritedisplayer.fragment.FragmentContents
import id.apwdevs.app.favoritedisplayer.plugin.Contracts
import id.apwdevs.app.favoritedisplayer.plugin.OnFragmentCallbacks
import id.apwdevs.app.favoritedisplayer.repository.MainListRepository
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity(), OnFragmentCallbacks {

    private var onPageChangeListener: ViewPager.OnPageChangeListener? = null
    private var observer: DObserver? = null
    private var hThread: HandlerThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setupTabs()
        hThread = HandlerThread("FavoriteDataObserver").apply {
            start()
            observer = DObserver(Handler(looper))
            observer?.let {
                contentResolver.registerContentObserver(Contracts.BASE_URI_FAVORITE.build(), true, it)
            }

        }
    }

    override fun onResume() {
        super.onResume()
        onPageChangeListener = object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                tabs?.getTabAt(position)?.select()
            }

        }
        onPageChangeListener?.let {
            view_pager?.addOnPageChangeListener(it)
        }

    }

    override fun onPause() {
        super.onPause()
        onPageChangeListener?.let {
            view_pager?.removeOnPageChangeListener(it)
        }
        onPageChangeListener = null
    }


    fun forceLoadFragment() {
        val adapter = view_pager.adapter as VAdapter
        val count = adapter.count
        for (i in 0 until count) {
            val fg = adapter.getItem(i)
            if (fg is FragmentContents) {
                fg.reload()
            }
        }
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
                        LayoutInflater.from(this@MainActivity).inflate(R.layout.tab_custom, tabs, false).apply {
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
        view_pager.offscreenPageLimit = 2
        view_pager.adapter = VAdapter(supportFragmentManager)
    }


    override fun onItemClicked(fg: Fragment, recyclerView: RecyclerView, position: Int, v: View) {
        sendBroadcast(Intent(FragmentContents.ACTION_LAUNCH_DETAIL).also {
            val adapter = recyclerView.adapter as ListAdapter
            val model = adapter.dataModel[position]
            it.putExtra(EXTRA_ID, model.id)
        })
    }

    override fun onRequestRefresh(fragment: Fragment) {

    }

    override fun onFavoriteChanged() {
    }

    override fun onDestroy() {
        super.onDestroy()
        observer?.let {
            contentResolver.unregisterContentObserver(it)
        }
        hThread?.quit()
        observer = null

        onPageChangeListener?.let {
            view_pager?.removeOnPageChangeListener(it)
        }
        onPageChangeListener = null
    }

    companion object {
        const val EXTRA_ID = "EXTRA_CONTENT_ID"
    }

    private inner class VAdapter(p: FragmentManager) :
        FragmentStatePagerAdapter(p, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val mFragments = listOf(
            FragmentContents.newInstance(MainListRepository.ContentDisplayType.MOVIE),
            FragmentContents.newInstance(MainListRepository.ContentDisplayType.TV_SHOWS)
        )

        override fun getItem(position: Int): Fragment = mFragments[position]

        override fun getCount(): Int = mFragments.size
    }

    inner class DObserver(handler: Handler) : ContentObserver(handler) {
        private val weakRef = WeakReference(this@MainActivity)
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            weakRef.get()?.forceLoadFragment()
        }
    }
}
