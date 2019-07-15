package id.apwdevs.app.catalogue.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.BitmapRequestListener
import com.kelin.translucentbar.library.TranslucentBarManager
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.adapter.DetailLayoutRecyclerAdapter
import id.apwdevs.app.catalogue.model.onDetail.SocmedIDModel
import id.apwdevs.app.catalogue.plugin.ProgressDialog
import id.apwdevs.app.catalogue.plugin.api.ApiRepository
import id.apwdevs.app.catalogue.plugin.api.GetImageFiles
import id.apwdevs.app.catalogue.plugin.view.ErrorSectionAdapter
import id.apwdevs.app.catalogue.view.MainDetailView
import id.apwdevs.app.catalogue.viewModel.DetailMovieViewModel
import id.apwdevs.app.catalogue.viewModel.DetailTVViewModel
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_detail_motion.*

class DetailActivity : AppCompatActivity(), MainDetailView {

    private lateinit var types: ContentTypes
    private lateinit var progress: ProgressDialog
    private lateinit var viewModel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_motion)
        AndroidNetworking.initialize(applicationContext)
        TranslucentBarManager(this).transparent(this)
        home.setOnClickListener {
            this@DetailActivity.finish()
        }

        if (savedInstanceState == null)
            intent.extras?.apply {
                types = getParcelable(EXTRA_DETAIL_TYPES)
            }
        else
            types = savedInstanceState.getParcelable(EXTRA_DETAIL_TYPES)
        progress = ProgressDialog(this)
        progress.text = "Wait for a few minutes"
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            0
        )

        when (types) {
            ContentTypes.ITEM_MOVIE -> {
                actdetail_title?.text = getString(R.string.detail_film_title)
                viewModel = ViewModelProviders.of(this).get(DetailMovieViewModel::class.java)
                viewModelAsDetailMovie()?.apply {
                    if (hasFirstInitialize.value == false) {
                        setAll(
                            this@DetailActivity,
                            ApiRepository(),
                            this@DetailActivity
                        )
                        hasFirstInitialize.value = true
                        return
                    } else {
                        compositingView(this)
                    }

                }
            }
            ContentTypes.ITEM_TV_SHOWS -> {
                actdetail_title?.text = getString(R.string.detail_tv_title)
                viewModel = ViewModelProviders.of(this).get(DetailTVViewModel::class.java)
                viewModelAsDetailTv()?.apply {
                    if (hasFirstInitialize.value == false) {
                        setAll(
                            this@DetailActivity,
                            ApiRepository(),
                            this@DetailActivity
                        )
                        hasFirstInitialize.value = true
                    } else {
                        compositingView(this)
                    }

                }
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelable(EXTRA_DETAIL_TYPES, types)
    }

    private fun viewModelAsDetailMovie(): DetailMovieViewModel? {
        if (viewModel is DetailMovieViewModel)
            return viewModel as DetailMovieViewModel
        return null
    }

    private fun viewModelAsDetailTv(): DetailTVViewModel? {
        if (viewModel is DetailTVViewModel)
            return viewModel as DetailTVViewModel
        return null
    }

    override fun onLoad() {
        progress.show()
    }

    override fun onLoadFailed(err: ApiRepository.RetError) {
        err.cause?.printStackTrace()
        progress.dismiss()
        AlertDialog.Builder(this).apply {
            val view = LayoutInflater.from(this@DetailActivity)
                .inflate(R.layout.error_layout, window.decorView as ViewGroup, false)
            setView(view)
            ErrorSectionAdapter(view).displayError(err)
            setCancelable(false)
            setNegativeButton("Go Back") { dialog, _ ->
                dialog.dismiss()
                this@DetailActivity.finish()
            }
        }.show()

    }

    override fun onLoadFinished(viewModel: ViewModel) {
        progress.dismiss()
        compositingView(viewModel)
    }

    private fun compositingView(viewModel: ViewModel) {
        val rectSize = Point()
        windowManager.defaultDisplay.getSize(rectSize)
        rectSize.y = resources.getDimension(R.dimen.actdetail_header_height).toInt()

        when (viewModel) {
            is DetailMovieViewModel -> {
                val creditsModel = viewModel.credits.value
                val data1Model = viewModel.details.value
                val data2Model = viewModel.otherDetails.value
                val reviews = viewModel.reviews.value
                val socmedIDModel = viewModel.socmedIds.value
                // sets the header
                // sets the image of header
                setBackdropPath(data1Model?.backdropPath, rectSize)
                /// sets the poster image
                setPosterImage(
                    data1Model?.posterPath, Point(
                        resources.getDimension(R.dimen.item_poster_width).toInt(),
                        resources.getDimension(R.dimen.item_poster_height).toInt()
                    )
                )
                /// sets the text title header
                data1Model?.let {
                    setTitleHeader(it.title, it.voteAverage, it.voteCount)
                }
                socmedIDModel?.let {
                    setSocmedId(it)
                }

                actdetail_recycler_content.apply {
                    layoutManager = LinearLayoutManager(this@DetailActivity)
                    adapter = DetailLayoutRecyclerAdapter(this@DetailActivity, ContentTypes.ITEM_MOVIE, Bundle().apply {
                        putParcelable(DetailLayoutRecyclerAdapter.EXTRA_DATA_1, data1Model)
                        putParcelable(DetailLayoutRecyclerAdapter.EXTRA_DATA_2, data2Model)
                        putParcelable(DetailLayoutRecyclerAdapter.EXTRA_DATA_REVIEWS, reviews)
                        putParcelableArrayList(DetailLayoutRecyclerAdapter.EXTRA_DATA_CREWS, creditsModel?.allCrew)
                        putParcelableArrayList(DetailLayoutRecyclerAdapter.EXTRA_DATA_CASTS, creditsModel?.allCasts)
                    }).apply {
                        onItemAction = object : DetailLayoutRecyclerAdapter.OnItemActionListener {
                            override fun onAction(viewType: DetailLayoutRecyclerAdapter.ViewType, vararg action: Any) {
                                Toast.makeText(this@DetailActivity, action[0].toString(), Toast.LENGTH_SHORT).show()
                                openTo(action[0].toString())
                            }

                        }
                    }

                }
            }
            is DetailTVViewModel -> {
                val creditsModel = viewModel.credits.value
                val data1Model = viewModel.shortDetails.value
                val data2Model = viewModel.otherDetails.value
                val reviews = viewModel.reviews.value
                val socmedIDModel = viewModel.socmedIds.value

                // sets the header
                // sets the image of header
                setBackdropPath(data1Model?.backdropPath, rectSize)
                /// sets the poster image
                setPosterImage(
                    data1Model?.posterPath, Point(
                        resources.getDimension(R.dimen.item_poster_width).toInt(),
                        resources.getDimension(R.dimen.item_poster_height).toInt()
                    )
                )
                /// sets the text title header
                data1Model?.let {
                    setTitleHeader(it.name, it.voteAverage, it.voteCount)
                }
                socmedIDModel?.let {
                    setSocmedId(it)
                }

                actdetail_recycler_content.apply {
                    layoutManager = LinearLayoutManager(this@DetailActivity)
                    adapter =
                        DetailLayoutRecyclerAdapter(this@DetailActivity, ContentTypes.ITEM_TV_SHOWS, Bundle().apply {
                            putParcelable(DetailLayoutRecyclerAdapter.EXTRA_DATA_1, data1Model)
                            putParcelable(DetailLayoutRecyclerAdapter.EXTRA_DATA_2, data2Model)
                            putParcelable(DetailLayoutRecyclerAdapter.EXTRA_DATA_REVIEWS, reviews)
                            putParcelableArrayList(DetailLayoutRecyclerAdapter.EXTRA_DATA_CREWS, creditsModel?.allCrew)
                            putParcelableArrayList(DetailLayoutRecyclerAdapter.EXTRA_DATA_CASTS, creditsModel?.allCasts)
                        })

                }
            }
        }
    }

    private fun setSocmedId(socmed: SocmedIDModel) {
        socmed.facebookId?.let { link ->
            avail_socmed.addImageIcon(R.drawable.ic_facebook_app_logo) {
                it.setOnClickListener {
                    openTo("https://www.facebook.com/$link")
                }
            }
        }
        socmed.instagramId?.let { link ->
            avail_socmed.addImageIcon(R.drawable.ic_instagram) {
                it.setOnClickListener {
                    openTo("https://www.instagram.com/$link")
                }
            }
        }
        socmed.twitterId?.let { link ->
            avail_socmed.addImageIcon(R.drawable.ic_twitter) {
                it.setOnClickListener {
                    openTo("https://www.twitter.com/$link")
                }
            }
        }
    }

    private fun openTo(link: String) {
        AlertDialog.Builder(this).apply {
            title = "Open Links"
            setMessage("Are you sure to open this page?")
            setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                startActivity(
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(link)
                    }
                )
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        }.show()
    }

    @SuppressLint("SetTextI18n")
    private fun setTitleHeader(title: CharSequence?, voteAverage: Double, voteCount: Int) {
        item_list_text_title.text = title
        item_list_ratingBar.rating = voteAverage.toFloat()
        item_list_votecount.text = "($voteCount)"

    }

    private fun setPosterImage(posterPath: String?, rectSize: Point) {
        posterPath?.let {
            AndroidNetworking.get(GetImageFiles.getImg(rectSize.x, it))
                .setPriority(Priority.LOW)
                .setImageScaleType(ImageView.ScaleType.FIT_XY)
                .setBitmapMaxWidth(rectSize.x)
                .setBitmapMaxHeight(rectSize.y)
                .build()
                .getAsBitmap(object : BitmapRequestListener {
                    override fun onResponse(response: Bitmap?) {
                        item_poster_image?.setImageBitmap(response)
                    }

                    override fun onError(anError: ANError?) {
                        Log.e("GetPosterImage", anError?.message, anError)
                    }

                })
        }
    }

    private fun setBackdropPath(path: String?, rectSize: Point) {
        path?.let {
            AndroidNetworking.get(GetImageFiles.getImg(rectSize.x, it))
                .setPriority(Priority.LOW)
                .setImageScaleType(ImageView.ScaleType.FIT_XY)
                .setBitmapMaxWidth(rectSize.x)
                .setBitmapMaxHeight(rectSize.y)
                .build()
                .getAsBitmap(object : BitmapRequestListener {
                    override fun onResponse(response: Bitmap?) {
                        actdetail_image_header?.setImageBitmap(response)
                        actdetail_image_header?.imageTintMode = PorterDuff.Mode.OVERLAY
                        actdetail_image_header?.imageTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(baseContext, R.color.image_header))
                    }

                    override fun onError(anError: ANError?) {
                        Log.e("GetBackdropPath", anError?.message, anError)
                    }

                })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_CONTENT_DETAILS = "CONTENT_DETAILS"
        const val EXTRA_ID = "ID_CONTENT"
        const val EXTRA_DETAIL_TYPES = "DETAIL_TYPES"

    }

    @Parcelize
    enum class ContentTypes : Parcelable {
        ITEM_MOVIE,
        ITEM_TV_SHOWS
    }
}
