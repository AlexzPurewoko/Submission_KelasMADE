package id.apwdevs.moTvCatalogue.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import id.apwdevs.moTvCatalogue.model.GenreModel
import id.apwdevs.moTvCatalogue.model.onUserMain.PageListModel
import id.apwdevs.moTvCatalogue.model.onUserMain.TvAboutModel
import id.apwdevs.moTvCatalogue.plugin.CoroutineContextProvider
import id.apwdevs.moTvCatalogue.plugin.api.ApiRepository
import id.apwdevs.moTvCatalogue.plugin.api.GetMovies
import id.apwdevs.moTvCatalogue.plugin.api.GetTVShows
import id.apwdevs.moTvCatalogue.plugin.jsonCheckAndGet
import id.apwdevs.moTvCatalogue.view.MainUserListView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class MainListTvViewModel : ViewModel() {
    val dataListObj: MutableLiveData<PageListModel> = MutableLiveData()
    val dataGenres: MutableLiveData<MutableList<GenreModel>> = MutableLiveData()

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
                view.onLoadFailed(it.errorCode, it.errorBody, it.cause)
                return@launch
            }
            getPages(apiRepository, types, pages, tag)?.let {
                view.onLoadFinished()
                view.onLoadFailed(it.errorCode, it.errorBody, it.cause)
                return@launch
            }
            view.onLoadFinished()
            view.onLoadSuccess(this@MainListTvViewModel)
        }
    }

    private suspend fun getAllGenre(apiRepository: ApiRepository): ANError? {
        apiRepository.doRequestAndReturnJSON(
            GetMovies.getAllGenre(),
            "GetAllTvGenre",
            Priority.MEDIUM
        ).await()?.let {
            if (it.isSuccess && !it.response.isNullOrEmpty()) {
                try {
                    JSONObject(it.response).getJSONArray("genres").apply {
                        val listGenre = mutableListOf<GenreModel>()
                        for (index in 0 until length()) {
                            getJSONObject(0).let { jsonObj ->
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
                    return ANError(e)
                }
            } else {
                return ANError(Exception("Is Not Success when get a response / response == null"))
            }
        }
        return ANError("Does Nothing, because get from apiRepository returned null")
    }

    private suspend fun getPages(
        apiRepository: ApiRepository,
        supportedType: SupportedType,
        pages: Int,
        tag: String
    ): ANError? {
        if (pages < 0 || pages > 1000) {
            return ANError(IllegalArgumentException("pages must between 0 and maxPages or 1000 : page $pages"))
        }
        dataListObj.value?.totalPages?.let {
            if (pages > it)
                return ANError(IllegalArgumentException("pages must between 0 and maxPages or 1000 : page $pages"))
        }
        apiRepository.doRequestAndReturnJSON(
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
                        val contents = mutableListOf<TvAboutModel>()
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
                    return ANError(e)
                }
            } else {
                return ANError(Exception("Is Not Success when get a response / response == null"))
            }
        }
        return ANError(Exception("Does Nothing, because get from apiRepository returned null"))
    }

    enum class SupportedType {
        DISCOVER,
        TV_AIRING_TODAY,
        TV_OTA,
        POPULAR,
        TOP_RATED
    }
}