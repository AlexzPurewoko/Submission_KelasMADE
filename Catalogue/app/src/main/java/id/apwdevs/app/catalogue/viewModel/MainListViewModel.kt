package id.apwdevs.app.catalogue.viewModel

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.common.Priority
import id.apwdevs.app.catalogue.model.GenreModel
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onUserMain.PageListModel
import id.apwdevs.app.catalogue.plugin.CoroutineContextProvider
import id.apwdevs.app.catalogue.plugin.PublicConfig
import id.apwdevs.app.catalogue.plugin.api.ApiRepository
import id.apwdevs.app.catalogue.plugin.api.GetMovies
import id.apwdevs.app.catalogue.plugin.api.GetTVShows
import id.apwdevs.app.catalogue.plugin.view.ErrorSectionAdapter
import id.apwdevs.app.catalogue.view.MainUserListView
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference

abstract class MainListViewModel : ViewModel() {
    protected val dataGenres: MutableLiveData<MutableList<GenreModel>> = MutableLiveData()
    protected val tag: MutableLiveData<String> = MutableLiveData()
    val dataListObj: MutableLiveData<PageListModel<ResettableItem>> = MutableLiveData()

    val hasFirstInstantiate: MutableLiveData<Boolean> = MutableLiveData()
    val prevListMode: MutableLiveData<Int> = MutableLiveData()
    val fragmentIsRefreshing: MutableLiveData<Boolean> = MutableLiveData()


    var context: WeakReference<Context>? = null
    var view: WeakReference<MainUserListView>? = null
    val apiRepository = ApiRepository()

    init {
        // we have to initialize these variables to be available for first instance
        hasFirstInstantiate.value = false
        fragmentIsRefreshing.value = false
        prevListMode.value = 0
    }

    fun setup(
        context: Context,
        view: MainUserListView,
        tag: String
    ) {
        this.context = WeakReference(context)
        this.view = WeakReference(view)
        this.tag.value = tag
    }

    protected abstract fun getTypes(): PublicConfig.ContentDisplayType

    abstract fun getAt(
        types: Parcelable, // get as MainListMovieViewModel.SupportedTypes or MaintListTvViewModel.SupportedTypes
        pages: Int,
        coroutineContextProvider: CoroutineContextProvider = CoroutineContextProvider()
    )

    protected suspend fun getAllGenre(apiRepository: ApiRepository): ApiRepository.RetError? {
        when (getTypes()) {
            PublicConfig.ContentDisplayType.MOVIE ->
                apiRepository.doReqAndRetResponseAsync(
                    context?.get(),
                    GetMovies.getAllGenre()
                    , "GetAllMovieGenre", Priority.MEDIUM
                ).await()
            PublicConfig.ContentDisplayType.TV_SHOWS ->
                apiRepository.doReqAndRetResponseAsync(
                    context?.get(),
                    GetTVShows.getAllGenre()
                    , "GetAllTvShowsGenre", Priority.MEDIUM
                ).await()
        }?.let {
            if (it.isSuccess && !it.response.isNullOrEmpty()) {
                try {
                    JSONObject(it.response).getJSONArray("genres").apply {
                        val listGenre = mutableListOf<GenreModel>()
                        for (index in 0 until length()) {
                            getJSONObject(index).let { jsonObj ->
                                listGenre.add(
                                    GenreModel(
                                        id = jsonObj.getInt("id"),
                                        genreName = jsonObj.getString("name")
                                    )
                                )
                            }

                        }
                        dataGenres.postValue(listGenre)
                    }
                    return null
                } catch (e: JSONException) {
                    e.printStackTrace()
                    return ApiRepository.RetError(ErrorSectionAdapter.ERR_CODE_PARSE_FAILED, e)
                }
            } else {
                return it.anErrorIfAny
            }
        }
        return ApiRepository.RetError(ErrorSectionAdapter.ERR_CODE_UNSPECIFIED, null)
    }
}