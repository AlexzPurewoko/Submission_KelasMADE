package id.apwdevs.app.catalogue.viewModel

import android.app.Application
import android.os.Parcelable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import id.apwdevs.app.catalogue.model.ClassResponse
import id.apwdevs.app.catalogue.model.GenreModel
import id.apwdevs.app.catalogue.model.onUserMain.MovieModelResponse
import id.apwdevs.app.catalogue.model.onUserMain.TvAboutModelResponse
import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer
import id.apwdevs.app.catalogue.repository.onUserMain.FragmentContentRepository
import kotlinx.android.parcel.Parcelize

@Suppress("UNCHECKED_CAST")
class MainListViewModel(application: Application) : AndroidViewModel(application) {
    val tag: MutableLiveData<String> = MutableLiveData()
    val hasFirstInstantiate: MutableLiveData<Boolean> = MutableLiveData()
    val prevListMode: MutableLiveData<Int> = MutableLiveData()
    val hasForceLoadContent: MutableLiveData<Boolean> = MutableLiveData()

    private var repository: FragmentContentRepository<ClassResponse>? = null

    var objData: LiveData<ClassResponse>? = null
    var isLoading: LiveData<Boolean>? = null
    var progress: LiveData<Double>? = null
    var allGenre: LiveData<List<GenreModel>>? = null
    var retError: LiveData<GetObjectFromServer.RetError>? = null
    init {
        // we have to initialize these variables to be available for first instance
        hasFirstInstantiate.value = false
        //fragmentIsRefreshing.value = false
        prevListMode.value = 0
        hasForceLoadContent.value = false
    }

    fun setup(
        tag: String,
        type: PublicContract.ContentDisplayType
    ) {
        repository = when (type) {
            PublicContract.ContentDisplayType.MOVIE ->
                FragmentContentRepository<MovieModelResponse>(
                    getApplication(),
                    type,
                    viewModelScope
                ) as FragmentContentRepository<ClassResponse>
            PublicContract.ContentDisplayType.TV_SHOWS ->
                FragmentContentRepository<TvAboutModelResponse>(
                    getApplication(),
                    type,
                    viewModelScope
                ) as FragmentContentRepository<ClassResponse>
            PublicContract.ContentDisplayType.FAVORITES ->
                FragmentContentRepository(
                    getApplication(),
                    PublicContract.ContentDisplayType.FAVORITES,
                    viewModelScope
                )

        }
        this.tag.value = tag
        isLoading = repository?.isLoading
        progress = repository?.progress
        allGenre = repository?.allGenre
        objData = repository?.objData
        retError = repository?.retError

    }

    fun getAt(
        types: Parcelable, // get as MovieTypeContract or TvTypeContract
        pages: Int
    ) {
        repository?.load(types)
    }

    @Parcelize
    enum class MovieTypeContract : Parcelable {
        DISCOVER,
        NOW_PLAYING,
        POPULAR,
        TOP_RATED,
        UPCOMING
    }

    @Parcelize
    enum class TvTypeContract : Parcelable {
        DISCOVER,
        TV_AIRING_TODAY,
        TV_OTA,
        POPULAR,
        TOP_RATED
    }
}