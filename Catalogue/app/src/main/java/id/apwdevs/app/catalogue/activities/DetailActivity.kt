package id.apwdevs.app.catalogue.activities

import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onUserMain.MovieAboutModel
import id.apwdevs.app.catalogue.model.onUserMain.TvAboutModel
import id.apwdevs.app.catalogue.plugin.ProgressDialog
import id.apwdevs.app.catalogue.plugin.api.ApiRepository
import id.apwdevs.app.catalogue.view.MainDetailView
import id.apwdevs.app.catalogue.viewModel.DetailMovieViewModel
import id.apwdevs.app.catalogue.viewModel.DetailTVViewModel
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity(), MainDetailView {

    private lateinit var types: ContentTypes
    private lateinit var progress: ProgressDialog
    private lateinit var contentAbout: ResettableItem

    private lateinit var viewModel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState == null)
            intent.apply {
                types = getParcelableExtra(EXTRA_DETAIL_TYPES)
                contentAbout = when (types) {
                    ContentTypes.ITEM_MOVIE ->
                        getParcelableExtra<MovieAboutModel>(EXTRA_CONTENT_DETAILS)
                    ContentTypes.ITEM_TV_SHOWS ->
                        getParcelableExtra<TvAboutModel>(EXTRA_CONTENT_DETAILS)
                } as ResettableItem
            }
        else
            types = savedInstanceState.getParcelable(EXTRA_DETAIL_TYPES)

        progress = ProgressDialog(this)

        when (types) {
            ContentTypes.ITEM_MOVIE -> {
                viewModel = ViewModelProviders.of(this).get(DetailMovieViewModel::class.java)
                viewModelAsDetailMovie()?.apply {
                    if (hasFirstInitialize.value == false) {
                        setAll(
                            this@DetailActivity,
                            ApiRepository(),
                            (contentAbout as MovieAboutModel).id,
                            this@DetailActivity
                        )
                        hasFirstInitialize.value = true
                    }
                }
            }
            ContentTypes.ITEM_TV_SHOWS -> {
                viewModel = ViewModelProviders.of(this).get(DetailTVViewModel::class.java)
                viewModelAsDetailTv()?.apply {
                    if (hasFirstInitialize.value == false) {
                        setAll(
                            this@DetailActivity,
                            ApiRepository(),
                            (contentAbout as TvAboutModel).idTv,
                            this@DetailActivity
                        )
                        hasFirstInitialize.value = true
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelable(EXTRA_DETAIL_TYPES, types)
    }

    fun viewModelAsDetailMovie(): DetailMovieViewModel? {
        if (viewModel is DetailMovieViewModel)
            return viewModel as DetailMovieViewModel
        return null
    }

    fun viewModelAsDetailTv(): DetailTVViewModel? {
        if (viewModel is DetailTVViewModel)
            return viewModel as DetailTVViewModel
        return null
    }

    override fun onLoad() {

    }

    override fun onLoadFailed(errorCode: Int, message: String?, cause: Throwable?) {
    }

    override fun onLoadFinished(viewModel: ViewModel) {

    }

    companion object {
        const val EXTRA_CONTENT_DETAILS = "CONTENT_DETAILS"
        const val EXTRA_DETAIL_TYPES = "DETAIL_TYPES"
        val TAG = DetailActivity::class.java.simpleName

    }

    @Parcelize
    enum class ContentTypes : Parcelable {
        ITEM_MOVIE,
        ITEM_TV_SHOWS
    }
}
