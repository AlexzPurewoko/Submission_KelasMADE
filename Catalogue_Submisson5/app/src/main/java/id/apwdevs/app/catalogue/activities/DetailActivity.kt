package id.apwdevs.app.catalogue.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Point
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jaeger.library.StatusBarUtil
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.adapter.DetailLayoutRecyclerAdapter
import id.apwdevs.app.catalogue.adapter.RecyclerCastsAdapter
import id.apwdevs.app.catalogue.adapter.RecyclerReviewAdapter
import id.apwdevs.app.catalogue.model.onDetail.SocmedIDModel
import id.apwdevs.app.catalogue.model.onUserMain.MainDataItemModel
import id.apwdevs.app.catalogue.plugin.DataObserver
import id.apwdevs.app.catalogue.plugin.ErrorAlertDialog
import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer
import id.apwdevs.app.catalogue.provider.FavoriteProvider
import id.apwdevs.app.catalogue.viewModel.DetailViewModel
import kotlinx.android.synthetic.main.activity_detail_motion.*

class DetailActivity : AppCompatActivity(), ErrorAlertDialog.OnErrorDialogBtnClickListener {

    private lateinit var viewModel: DetailViewModel
    private lateinit var loadSnackbar: Snackbar
    private lateinit var mTextProgress: TextView
    private var isFirstLaunched: Boolean = true
    private var mContentHandlerThread: HandlerThread? = null
    private var mObserver: DataObserver? = null

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
            finishTask()
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
        setSnackbar()
        confViewModel()
        actdetail_favorite.setOnClickListener {
            viewModel.isAnyChangesMade.value = true
            viewModel.onClickFavoriteBtn(it)

        }
        setupObserver()
    }

    override fun onDestroy() {
        super.onDestroy()
        mObserver?.let {
            contentResolver.unregisterContentObserver(it)
        }
        mContentHandlerThread?.quit()
        mObserver = null
        mContentHandlerThread = null
    }

    private fun setupObserver() {
        mContentHandlerThread = HandlerThread("DetailDataObserver").apply {
            start()
            mObserver = DataObserver(Handler(looper)) {
                viewModel.onDataChanged()
            }
            mObserver?.let {
                contentResolver.registerContentObserver(FavoriteProvider.BASE_URI_FAVORITE.build(), true, it)
            }
        }
    }

    private fun setSnackbar() {
        loadSnackbar = Snackbar.make(det_container, "", Snackbar.LENGTH_INDEFINITE)
        val inflate = LayoutInflater.from(this).inflate(R.layout.adapter_loading, det_container as ViewGroup, false)
        mTextProgress = inflate.findViewById(R.id.progress_text)
        (loadSnackbar.view as FrameLayout).addView(inflate)
    }

    private fun finishTask() {

        setResult(
            when (viewModel.isAnyChangesMade.value) {
                true -> MainTabUserActivity.LAYOUT_REQUEST_UPDATE
                else -> MainTabUserActivity.NO_REQUEST // false & null returned value
            }
        )
        this@DetailActivity.finish()
    }

    private fun confViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        ).get(DetailViewModel::class.java)

        viewModel.apply {
            if (hasFirstInitialize.value == false)
                setup(intent)

            actdetail_title?.text =
                when (types.value) {
                    PublicContract.ContentDisplayType.MOVIE -> {
                        getString(R.string.detail_film_title)
                    }
                    PublicContract.ContentDisplayType.TV_SHOWS -> {
                        getString(R.string.detail_tv_title)
                    }
                    // will be changed again
                    PublicContract.ContentDisplayType.FAVORITES -> {
                        "Favorites"
                    }
                    else -> ""
                }

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

            hasLoading.observe(this@DetailActivity, Observer {
                runOnUiThread {
                    when (it) {
                        false -> {
                        }
                        true -> {
                            loadSnackbar.show()
                        }
                    }
                    actdetail_recycler_content.adapter?.notifyDataSetChanged()
                }
            })

            retError.observe(this@DetailActivity, Observer {
                it?.let { _ ->
                    it.cause?.printStackTrace()
                    ErrorAlertDialog().apply {
                        returnedError = it
                        isCancelable = false
                    }.showNow(supportFragmentManager, "ErrorDialog")
                }

            })

            socmedIds.observe(this@DetailActivity, Observer {
                setSocmedId(it)
            })

            isFavorite.observe(this@DetailActivity, Observer {
                actdetail_favorite.setImageResource(
                    if (it)
                        R.drawable.ic_favorite_activated_24dp
                    else
                        R.drawable.ic_favorite_border_24dp
                )
                if (!isFirstLaunched) {
                    Snackbar.make(
                        det_container, when (it) {
                            true -> this@DetailActivity.getString(R.string.add_fav)
                            false -> this@DetailActivity.getString(R.string.remove_fav)
                        }, Snackbar.LENGTH_SHORT
                    ).show()
                }
                isFirstLaunched = false
            })

            data1Obj.observe(this@DetailActivity, Observer {
                var title: CharSequence? = null
                var backdropPath: String? = null
                var posterPath: String? = null
                var voteAverage: Double? = null
                var voteCount: Int? = null
                it.apply {
                    if (this is MainDataItemModel) {
                        title = this.title
                        backdropPath = this.backdropPath
                        posterPath = this.posterPath
                        voteAverage = this.voteAverage
                        voteCount = this.voteCount
                    }
                }
                // sets the header
                // sets the image of header
                setBackdropPath(backdropPath, getHeaderRectSize(this@DetailActivity))
                /// sets the poster image
                setPosterImage(
                    posterPath, Point(
                        resources.getDimension(R.dimen.item_poster_width).toInt(),
                        resources.getDimension(R.dimen.item_poster_height).toInt()
                    )
                )
                /// sets the text title header
                setTitleHeader(title, requireNotNull(voteAverage), requireNotNull(voteCount))
            })

            progress.observe(this@DetailActivity, Observer {
                if (loadFinished.value == false && hasLoading.value == true)
                    mTextProgress.text = getString(R.string.loading, it.toInt())
            })

            loadFinished.observe(this@DetailActivity, Observer {
                if (it) {
                    loadSnackbar.dismiss()
                    setRecycler(this)
                }
            })
            if (hasFirstInitialize.value == false) {
                hasFirstInitialize.value = true
                loadData()
                return
            }
        }
    }

    private fun setRecycler(viewModel: DetailViewModel) {
        val maxRevResults = viewModel.maxAllowedReviewsResult.value ?: RecyclerReviewAdapter.DEFAULT_LIMITS
        val maxCreditsResults = viewModel.maxAllowedCreditsResult.value ?: RecyclerCastsAdapter.DEFAULT_LIMITS
        actdetail_recycler_content.layoutManager = LinearLayoutManager(this)
        actdetail_recycler_content.adapter =
            DetailLayoutRecyclerAdapter(
                this@DetailActivity,
                viewModel.typeContent,
                viewModel,
                maxRevResults,
                maxCreditsResults
            ).apply {
                onItemAction = object : DetailLayoutRecyclerAdapter.OnItemActionListener {
                    override fun onAction(viewType: DetailLayoutRecyclerAdapter.ViewType, vararg action: Any) {
                        openTo(action[0].toString())
                    }

                }
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
        if (avail_socmed.childCount == 0) {
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
            GetObjectFromServer.getInstance(this).getBitmapNoProgress(rectSize, it) { bitmap ->
                item_poster_image?.setImageBitmap(bitmap)
            }
        }
    }

    private fun setBackdropPath(path: String?, rectSize: Point) {
        path?.let {
            GetObjectFromServer.getInstance(this)
                .getBitmapNoProgress(rectSize, it, true, ImageView.ScaleType.FIT_XY) { bitmap ->
                    actdetail_image_header?.setImageBitmap(bitmap)
                }
        }
    }

    override fun onBackPressed() {
        finishTask()
    }

    override fun onRequestRefresh(errorAlertDialog: ErrorAlertDialog) {
        errorAlertDialog.dismiss()
        viewModel.loadData()
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
