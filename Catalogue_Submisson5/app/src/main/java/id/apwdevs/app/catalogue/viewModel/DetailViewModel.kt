package id.apwdevs.app.catalogue.viewModel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.activities.DetailActivity
import id.apwdevs.app.catalogue.adapter.RecyclerCastsAdapter
import id.apwdevs.app.catalogue.adapter.RecyclerReviewAdapter
import id.apwdevs.app.catalogue.database.FavoriteDatabase
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onDetail.CreditsModel
import id.apwdevs.app.catalogue.model.onDetail.ReviewResponse
import id.apwdevs.app.catalogue.model.onDetail.SocmedIDModel
import id.apwdevs.app.catalogue.model.onUserMain.MainDataItemModel
import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer
import id.apwdevs.app.catalogue.plugin.configureFavorite
import id.apwdevs.app.catalogue.repository.onDetail.DetailActivityRepository
import id.apwdevs.app.catalogue.repository.onDetail.DetailFavoriteActRepo
import id.apwdevs.app.catalogue.repository.onDetail.DetailMovieActRepo
import id.apwdevs.app.catalogue.repository.onDetail.DetailTvActRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {
    val id: MutableLiveData<Int> = MutableLiveData()
    val hasFirstInitialize: MutableLiveData<Boolean> = MutableLiveData()
    val hasOverlayMode: MutableLiveData<Boolean> = MutableLiveData()
    val isAnyChangesMade: MutableLiveData<Boolean> = MutableLiveData()
    val types: MutableLiveData<PublicContract.ContentDisplayType> = MutableLiveData()

    val maxAllowedCreditsResult: MutableLiveData<Int> = MutableLiveData()
    val maxAllowedReviewsResult: MutableLiveData<Int> = MutableLiveData()


    private lateinit var repository: DetailActivityRepository
    lateinit var socmedIds: LiveData<SocmedIDModel>
    lateinit var reviews: LiveData<ReviewResponse>
    lateinit var credits: LiveData<CreditsModel>
    lateinit var hasLoading: LiveData<Boolean>
    lateinit var data1Obj: LiveData<ResettableItem>
    lateinit var data2Obj: LiveData<ResettableItem>
    lateinit var retError: LiveData<GetObjectFromServer.RetError>
    lateinit var typeContent: DetailActivityRepository.TypeContentContract
    lateinit var loadFinished: LiveData<Boolean>
    lateinit var isFavorite: MutableLiveData<Boolean>
    lateinit var progress: MutableLiveData<Float>

    init {
        hasFirstInitialize.value = false
        hasOverlayMode.value = false
        isAnyChangesMade.value = false
    }

    // this will called at first of viewModel has launched
    fun setup(dataIntent: Intent) {
        dataIntent.extras?.apply {
            types.value =
                getParcelable<PublicContract.ContentDisplayType>(DetailActivity.EXTRA_DETAIL_TYPES)?.also { type ->
                    repository = when (type) {
                        PublicContract.ContentDisplayType.FAVORITES -> DetailFavoriteActRepo(
                            getApplication(),
                            viewModelScope
                        )
                        PublicContract.ContentDisplayType.MOVIE -> DetailMovieActRepo(getApplication(), viewModelScope)
                        PublicContract.ContentDisplayType.TV_SHOWS -> DetailTvActRepo(getApplication(), viewModelScope)
                    }
                    socmedIds = repository.socmedIds
                    reviews = repository.reviews
                    credits = repository.credits
                    hasLoading = repository.hasLoading
                    data1Obj = repository.data1Obj
                    data2Obj = repository.data2Obj
                    repository.initAtFirstTime(dataIntent)
                    retError = repository.retError
                    typeContent = repository.typeContentContract
                    loadFinished = repository.loadFinished
                    isFavorite = repository.isFavorite
                    progress = repository.progress
                }
            id.value = getInt(DetailActivity.EXTRA_ID)
        }
    }

    fun loadData() {
        id.value?.let {
            val ctx: Context = getApplication()
            ctx.getSharedPreferences(PublicContract.SHARED_PREF_GLOBAL_NAME, Context.MODE_PRIVATE).let { shared ->
                val creditsVal = shared.getString("max_credits_results", "10")
                val reviewsVal = shared.getString("max_review_results", "5")
                maxAllowedCreditsResult.value =
                    when (creditsVal) {
                        "max" -> RecyclerCastsAdapter.NO_LIMITS
                        null -> 10
                        else -> creditsVal.toInt()
                    }
                maxAllowedReviewsResult.value = when (reviewsVal) {
                    "max" -> RecyclerReviewAdapter.NO_LIMITS
                    null -> 5
                    else -> reviewsVal.toInt()
                }
            }
            repository.load(it)
        }
    }

    fun getHeaderRectSize(activity: AppCompatActivity): Point {
        val rectSize = Point()
        activity.windowManager.defaultDisplay.getSize(rectSize)
        rectSize.y = activity.resources.getDimension(R.dimen.actdetail_header_height).toInt()
        return rectSize
    }

    fun onClickFavoriteBtn(v: View?, posterDrawable: Drawable?) {
        if (v == null) return
        viewModelScope.launch(Dispatchers.IO) {
            val fav = configureFavorite(v.context, data1Obj.value, posterDrawable)
            isFavorite.postValue(fav)
        }
    }

    fun onDataChanged() {
        val favoriteDao = FavoriteDatabase.getInstance(getApplication()).favoriteDao()
        id.value?.let {
            val isFav = favoriteDao.isAnyColumnIn(it)
            isFavorite.postValue(isFav)
            isAnyChangesMade.postValue(true)
            viewModelScope.launch(Dispatchers.Main) {
                data1Obj.value?.let { item ->
                    if (item is MainDataItemModel)
                        item.isFavorite = isFav
                }
            }
        }
    }

}