package id.apwdevs.app.catalogue.viewModel

import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.common.Priority
import id.apwdevs.app.catalogue.model.GenreModel
import id.apwdevs.app.catalogue.model.onUserMain.PageListModel
import id.apwdevs.app.catalogue.model.onUserMain.TvAboutModel
import id.apwdevs.app.catalogue.plugin.CoroutineContextProvider
import id.apwdevs.app.catalogue.plugin.PublicConfig
import id.apwdevs.app.catalogue.plugin.api.ApiRepository
import id.apwdevs.app.catalogue.plugin.api.GetTVShows
import id.apwdevs.app.catalogue.plugin.jsonCheckAndGet
import id.apwdevs.app.catalogue.plugin.view.ErrorSectionAdapter
import id.apwdevs.app.catalogue.view.MainUserListView
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class MainListTvViewModel : ViewModel() {

    val hasFirstInstantiate : MutableLiveData<Boolean> = MutableLiveData()
    val currentListMode : MutableLiveData<Int> = MutableLiveData()
    val dataListObj: MutableLiveData<PageListModel<TvAboutModel>> = MutableLiveData()
    private val dataGenres: MutableLiveData<MutableList<GenreModel>> = MutableLiveData()

    init {
        hasFirstInstantiate.value = false
        currentListMode.value = PublicConfig.RecyclerMode.MODE_LIST
    }
    fun setup(
        apiRepository: ApiRepository,
        types: SupportedType,
        pages: Int,
        tag: String,
        view: MainUserListView,
        coroutineContextProvider: CoroutineContextProvider = CoroutineContextProvider()
    ) {
        view.onStart()
        GlobalScope.launch(coroutineContextProvider.main) {
            getAllGenre(apiRepository)?.let {
                view.onLoadFinished()
                view.onLoadFailed(it)
                return@launch
            }
            getPages(apiRepository, types, pages, tag)?.let {
                view.onLoadFinished()
                view.onLoadFailed(it)
                return@launch
            }
            view.onLoadFinished()
            view.onLoadSuccess(this@MainListTvViewModel)
        }
    }

    private suspend fun getAllGenre(apiRepository: ApiRepository): ApiRepository.RetError? {
        apiRepository.doReqAndRetResponseAsync(
            GetTVShows.getAllGenre(),
            "GetAllTvGenre",
            Priority.LOW
        ).await()?.let {
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

    private suspend fun getPages(
        apiRepository: ApiRepository,
        supportedType: SupportedType,
        pages: Int,
        tag: String
    ): ApiRepository.RetError? {
        if (pages < 0 || pages > 1000) {
            return ApiRepository.RetError(ErrorSectionAdapter.ERR_CODE_PARSE_FAILED, IllegalArgumentException("pages must between 0 and maxPages or 1000 : page $pages"))
        }
        /*dataListObj.value.totalPages.apply {
            if (pages > it)
                return ANError(IllegalArgumentException("pages must between 0 and maxPages or 1000 : page $pages"))
        }*/
        apiRepository.doReqAndRetResponseAsync(
            GetTVShows.getList(
                supportedType,
                pages
            ),
            "${tag}Pages$pages",
            Priority.LOW
        ).await()?.let {
            if (it.isSuccess && !it.response.isNullOrEmpty()) {
                try {
                    JSONObject(it.response).apply {

                        val inPage = getInt("page")
                        val totalResults = getInt("total_results")
                        val totalPages = getInt("total_pages")
                        val contents = arrayListOf<TvAboutModel>()
                        //inflate the contents
                        val jsonContents = getJSONArray("results")
                        for (index in 0 until jsonContents.length()) {
                            jsonContents.getJSONObject(index).apply {
                                val genres = mutableListOf<GenreModel>()
                                val jsonGenres = getJSONArray("genre_ids")
                                for (genreIdx in 0 until jsonGenres.length()) {
                                    val id = jsonGenres.getInt(genreIdx)
                                    dataGenres.value?.forEach { data ->
                                        if (data.id == id)
                                            genres.add(
                                                data.copy()
                                            )
                                    }

                                }
                                val retOriginCountry = mutableListOf<String>()
                                val jsonOrCountry = getJSONArray("origin_country")
                                for (idx in 0 until jsonOrCountry.length()) {
                                    retOriginCountry.add(jsonOrCountry.getString(idx))
                                }
                                contents.add(
                                    TvAboutModel(
                                        idTv = getInt("id"),
                                        firstAirDate = getString("first_air_date"),
                                        posterPath = jsonCheckAndGet(get("poster_path"))?.toString(),
                                        overview = getString("overview"),
                                        genres = genres,
                                        originalLanguage = getString("original_language"),
                                        name = getString("name"),
                                        backdropPath = jsonCheckAndGet(get("backdrop_path"))?.toString(),
                                        popularity = getDouble("popularity"),
                                        voteCount = getInt("vote_count"),
                                        voteAverage = getDouble("vote_average"),
                                        originCountry = retOriginCountry,
                                        originalName = getString("original_name")
                                    )
                                )
                            }

                        }
                        dataListObj.postValue(
                            PageListModel(
                                inPage = inPage,
                                totalPages = totalPages,
                                totalResults = totalResults,
                                contents = contents,
                                errorCode = 0,
                                errorMessage = null

                            )
                        )
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

    @Parcelize
    enum class SupportedType : Parcelable{
        DISCOVER,
        TV_AIRING_TODAY,
        TV_OTA,
        POPULAR,
        TOP_RATED
    }
}