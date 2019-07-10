package id.apwdevs.moTvCatalogue.activities

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.apwdevs.moTvCatalogue.R
import id.apwdevs.moTvCatalogue.adapter.AboutAdapter
import id.apwdevs.moTvCatalogue.adapter.TopBilledCastAdapter
import id.apwdevs.moTvCatalogue.model.OtherAboutFilmModel
import id.apwdevs.moTvCatalogue.model.OtherAboutTVModel
import id.apwdevs.moTvCatalogue.model.ShortListModel
import id.apwdevs.moTvCatalogue.presenter.MainDetailMovieOrTvPresenter
import id.apwdevs.moTvCatalogue.view.MainDetailMovieView
import kotlinx.android.synthetic.main.activity_detail_movies_or_tv.*

class DetailMovieOrTv : AppCompatActivity(), MainDetailMovieView {
    private var currentModes = 0

    private var aboutAdapter: AboutAdapter? = null
    private var topBilledCastAdapter: TopBilledCastAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_movies_or_tv)

        var moviePosition: Int

        intent.apply {
            val shortListModel = getParcelableExtra<ShortListModel>(
                EXTRA_MOVIE_OR_TV_DATA
            )
            moviePosition = getIntExtra(EXTRA_MOVIE_OR_TV_NUM, 0)
            currentModes = getIntExtra(EXTRA_MODES, 0)
            shortListModel.apply {
                image_movies.setImageResource(photoRes)
                text_movie_title.text = title
                text_released.text = releaseDate
                detail_text_overview.text = overview
            }
        }

        aboutAdapter = AboutAdapter(this)
        topBilledCastAdapter = TopBilledCastAdapter(this)
        list_side_left_recycler.apply {
            adapter = aboutAdapter
            layoutManager = LinearLayoutManager(this@DetailMovieOrTv)
        }
        val mainDetailMovieOrTvPresenter = MainDetailMovieOrTvPresenter(this, this)
        mainDetailMovieOrTvPresenter.prepareAll(moviePosition, currentModes)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        newConfig?.apply {
            when (orientation) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ->
                    list_side_left_recycler.isNestedScrollingEnabled = true
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ->
                    list_side_left_recycler.isNestedScrollingEnabled = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (supportActionBar != null) {
            when (currentModes) {
                MODE_MOVIE -> supportActionBar?.title = getString(R.string.act_detail_movie)
                MODE_TV -> supportActionBar?.title = getString(R.string.act_detail_tv)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (currentModes == MODE_MOVIE) {
            val menuInflater = menuInflater
            menuInflater.inflate(R.menu.menu_detail, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (currentModes == MODE_MOVIE) {
            if (item.itemId == R.id.menu_list_actor) {
                val listView = RecyclerView(this).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    layoutManager = LinearLayoutManager(this@DetailMovieOrTv)
                    setPadding(16, 16, 16, 16)
                    adapter = topBilledCastAdapter
                }

                AlertDialog.Builder(this).apply {
                    setTitle("Top Billed Cast")
                    setView(listView)
                    setPositiveButton(
                        "Okay"
                    ) { dialog, _ -> dialog.dismiss() }
                }.show()
                Toast.makeText(this, "List Actor", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onLoadData() {

    }

    override fun onLoadFinished(dataModels: Any?) {
        when (currentModes) {
            MODE_MOVIE -> {
                val otherAboutFilmModel = dataModels as OtherAboutFilmModel?
                aboutAdapter?.setData(otherAboutFilmModel)
                topBilledCastAdapter?.filmTopBilledCastModels = otherAboutFilmModel?.listTopBilledCast
            }
            MODE_TV -> aboutAdapter?.setData((dataModels as OtherAboutTVModel?))
        }

    }

    companion object {

        const val EXTRA_MOVIE_OR_TV_DATA = "MOVIE_OR_TV_DATA"
        const val EXTRA_MOVIE_OR_TV_NUM = "MOVIE_OR_TV_POS"
        const val EXTRA_MODES = "CLASS_MODES"
        const val MODE_MOVIE = 0x6ffa
        const val MODE_TV = 0xfab56
    }
}