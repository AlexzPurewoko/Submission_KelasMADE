package id.apwdevs.app.catalogue.viewModel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import id.apwdevs.app.catalogue.activities.DetailActivity
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onDetail.CreditsModel
import id.apwdevs.app.catalogue.model.onDetail.ReviewResponse
import id.apwdevs.app.catalogue.model.onDetail.SocmedIDModel
import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer
import id.apwdevs.app.catalogue.repository.onDetail.DetailActivityRepository
import id.apwdevs.app.catalogue.repository.onDetail.DetailFavoriteActRepo
import id.apwdevs.app.catalogue.repository.onDetail.DetailMovieActRepo
import id.apwdevs.app.catalogue.repository.onDetail.DetailTvActRepo

class DetailViewModel(application: Application) : AndroidViewModel(application) {
    val id: MutableLiveData<Int> = MutableLiveData()
    val hasFirstInitialize: MutableLiveData<Boolean> = MutableLiveData()
    val hasOverlayMode: MutableLiveData<Boolean> = MutableLiveData()
    val types: MutableLiveData<PublicContract.ContentDisplayType> = MutableLiveData()

    private lateinit var repository: DetailActivityRepository
    lateinit var socmedIds: LiveData<SocmedIDModel>
    lateinit var reviews: LiveData<ReviewResponse>
    lateinit var credits: LiveData<CreditsModel>
    lateinit var hasLoading: LiveData<Boolean>
    lateinit var data1Obj: LiveData<ResettableItem>
    lateinit var data2Obj: LiveData<ResettableItem>
    lateinit var retError: LiveData<GetObjectFromServer.RetError>
    lateinit var typeContent: DetailActivityRepository.TypeContentContract

    init {
        hasFirstInitialize.value = false
        hasOverlayMode.value = false
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
                }
            id.value = getInt(DetailActivity.EXTRA_ID)
        }
    }

    fun loadData() {
        id.value?.let {
            repository.load(it)
        }
    }

}