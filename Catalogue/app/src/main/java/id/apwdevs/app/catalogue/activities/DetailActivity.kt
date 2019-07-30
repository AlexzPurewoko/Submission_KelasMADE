package id.apwdevs.app.catalogue.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Point
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jaeger.library.StatusBarUtil
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.adapter.DetailLayoutRecyclerAdapter
import id.apwdevs.app.catalogue.model.onDetail.SocmedIDModel
import id.apwdevs.app.catalogue.plugin.ErrorAlertDialog
import id.apwdevs.app.catalogue.plugin.ProgressDialog
import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.plugin.api.ApiRepository
import id.apwdevs.app.catalogue.plugin.getBitmap
import id.apwdevs.app.catalogue.view.MainDetailView
import id.apwdevs.app.catalogue.viewModel.DetailMovieViewModel
import id.apwdevs.app.catalogue.viewModel.DetailTVViewModel
import id.apwdevs.app.catalogue.viewModel.DetailViewModel
import kotlinx.android.synthetic.main.activity_detail_motion.*

class DetailActivity : AppCompatActivity(), MainDetailView, ErrorAlertDialog.OnErrorDialogBtnClickListener {

    private lateinit var types: PublicContract.ContentDisplayType
    private lateinit var progress: ProgressDialog
    private lateinit var viewModel: DetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_motion)
        // transparent the status bar
        if (resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        } else {
            StatusBarUtil.setTransparent(this)
        }
        // when clicking home button, this context will be pop back from stack
        home.setOnClickListener {
            this@DetailActivity.finish()
        }
        actdetail_tint?.setOnClickListener {
            viewModel.hasOverlayMode.value = !(viewModel.hasOverlayMode.value ?: false)
        }
        actdetail_tint?.setOnLongClickListener {
            val tag = it.tag
            if (tag is String)
                Toast.makeText(this@DetailActivity, tag, Toast.LENGTH_SHORT).show()
            true
        }

        if (savedInstanceState == null)
            intent.extras?.apply {
                types = getParcelable(EXTRA_DETAIL_TYPES)
            }
        else
            types = savedInstanceState.getParcelable(EXTRA_DETAIL_TYPES)
        progress = ProgressDialog()

        // gets the title and viewModel
        actdetail_title?.text =
            when (types) {
                PublicContract.ContentDisplayType.MOVIE -> {
                    viewModel = ViewModelProviders.of(this).get(DetailMovieViewModel::class.java)
                    getString(R.string.detail_film_title)
                }
                PublicContract.ContentDisplayType.TV_SHOWS -> {
                    viewModel = ViewModelProviders.of(this).get(DetailTVViewModel::class.java)
                    getString(R.string.detail_tv_title)
                }

                // will be changed again
                PublicContract.ContentDisplayType.FAVORITES -> {
                    viewModel = ViewModelProviders.of(this).get(DetailTVViewModel::class.java)
                    "Favorites"
                }
            }

        // initialize and observe the viewmodel
        viewModel.setup(this, this)
        viewModel.apply {
            hasLoading.observe(this@DetailActivity, Observer {
                runOnUiThread {
                    when (it) {
                        false -> {
                            try {
                                progress.dismiss()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        true -> {
                            progress.show(supportFragmentManager, null)
                        }
                    }
                    actdetail_recycler_content.adapter?.notifyDataSetChanged()
                }
            })
            hasOverlayMode.observe(this@DetailActivity, Observer {
                actdetail_image_header?.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(baseContext, R.color.image_header))
                if (it) {
                    actdetail_image_header?.imageTintMode = PorterDuff.Mode.DARKEN
                    actdetail_tint?.apply {
                        setImageResource(R.drawable.ic_tint_overlay_off_24dp)
                        tag = context.getString(R.string.img_tint_mode_overlay_tag)
                    }

                } else {
                    actdetail_image_header?.imageTintMode = PorterDuff.Mode.OVERLAY
                    actdetail_tint?.apply {
                        setImageResource(R.drawable.ic_tint_overlay_24dp)
                        tag = context.getString(R.string.img_tint_mode_darken_tag)
                    }
                }

            })

            if (hasFirstInitialize.value == false) {
                hasFirstInitialize.value = true
                getAll()
                return
            } else {
                if (hasLoading.value != true && (loadSuccess.value == true))
                    compositingView(this)
            }
        }

    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EXTRA_DETAIL_TYPES, types)
    }

    override fun onLoad() {
    }

    override fun onLoadFailed(err: ApiRepository.RetError) {
        err.cause?.printStackTrace()
        viewModel.hasLoading.value = false
        ErrorAlertDialog().apply {
            returnedError = err
            isCancelable = false
        }.showNow(supportFragmentManager, "ErrorDialog")

    }

    override fun onLoadFinished(viewModel: DetailViewModel) {
        compositingView(viewModel)
    }

    private fun compositingView(viewModel: DetailViewModel) {
        val rectSize = Point()
        windowManager.defaultDisplay.getSize(rectSize)
        rectSize.y = resources.getDimension(R.dimen.actdetail_header_height).toInt()
        actdetail_recycler_content.layoutManager = LinearLayoutManager(this)
        actdetail_recycler_content.adapter =
            DetailLayoutRecyclerAdapter(this@DetailActivity, types, viewModel).apply {
                onItemAction = object : DetailLayoutRecyclerAdapter.OnItemActionListener {
                    override fun onAction(viewType: DetailLayoutRecyclerAdapter.ViewType, vararg action: Any) {
                        openTo(action[0].toString())
                    }

                }
            }

        val socmedIDModel = viewModel.socmedIds.value
        var title: CharSequence? = null
        var backdropPath: String? = null
        var posterPath: String? = null
        var voteAverage: Double? = null
        var voteCount: Int? = null
        when (viewModel) {
            is DetailMovieViewModel -> {
                val data1Model = viewModel.details.value
                title = data1Model?.title
                backdropPath = data1Model?.backdropPath
                posterPath = data1Model?.posterPath
                voteAverage = data1Model?.voteAverage
                voteCount = data1Model?.voteCount
            }
            is DetailTVViewModel -> {
                val data1Model = viewModel.shortDetails.value
                title = data1Model?.name
                backdropPath = data1Model?.backdropPath
                posterPath = data1Model?.posterPath
                voteAverage = data1Model?.voteAverage
                voteCount = data1Model?.voteCount
            }
        }

        // sets the header
        // sets the image of header
        setBackdropPath(backdropPath, rectSize)
        /// sets the poster image
        setPosterImage(
            posterPath, Point(
                resources.getDimension(R.dimen.item_poster_width).toInt(),
                resources.getDimension(R.dimen.item_poster_height).toInt()
            )
        )
        /// sets the text title header
        setTitleHeader(title, requireNotNull(voteAverage), requireNotNull(voteCount))
        socmedIDModel?.let {
            setSocmedId(it)
        }
        actdetail_recycler_content.layoutManager?.smoothScrollToPosition(
            actdetail_recycler_content,
            RecyclerView.State().apply {
                willRunPredictiveAnimations()
            },
            0
        )
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
            title = getString(R.string.open_links)
            setMessage(getString(R.string.opento_message))
            setPositiveButton(getString(R.string.true_text)) { dialog, _ ->
                dialog.dismiss()
                startActivity(
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(link)
                    }
                )
            }
            setNegativeButton(R.string.false_text) { dialog, _ ->
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
            getBitmap(rectSize, it) { bitmap ->
                item_poster_image?.setImageBitmap(bitmap)
            }
        }
    }

    private fun setBackdropPath(path: String?, rectSize: Point) {
        path?.let {
            getBitmap(rectSize, it) { bitmap ->
                actdetail_image_header?.setImageBitmap(bitmap)
            }
        }
    }

    override fun onRequestRefresh(errorAlertDialog: ErrorAlertDialog) {
        errorAlertDialog.dismiss()
        viewModel.getAll()
    }

    override fun onRequestBack(errorAlertDialog: ErrorAlertDialog) {
        errorAlertDialog.dismiss()
        finish()
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
}
