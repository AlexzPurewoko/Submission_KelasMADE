package id.apwdevs.app.catalogue.viewModel

import android.os.Parcelable
import com.androidnetworking.common.Priority
import id.apwdevs.app.catalogue.model.GenreModel
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onUserMain.MovieAboutModel
import id.apwdevs.app.catalogue.model.onUserMain.PageListModel
import id.apwdevs.app.catalogue.plugin.CoroutineContextProvider
import id.apwdevs.app.catalogue.plugin.PublicConfig
import id.apwdevs.app.catalogue.plugin.api.ApiRepository
import id.apwdevs.app.catalogue.plugin.api.GetMovies
import id.apwdevs.app.catalogue.plugin.jsonCheckAndGet
import id.apwdevs.app.catalogue.plugin.view.ErrorSectionAdapter
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class MainListMovieViewModel : MainListViewModel() {

    override fun getAt(
        types: Parcelable,
        pages: Int,
        coroutineContextProvider: CoroutineContextProvider
    ) {
        if (types is SupportedType) {
            view?.get()?.onStart()
            GlobalScope.launch(coroutineContextProvider.main) {
                getAllGenre(apiRepository)?.let {
                    view?.get()?.onLoadFinished()
                    view?.get()?.onLoadFailed(it)
                    return@launch
                }
                getPages(apiRepository, types, pages, tag.value)?.let {
                    view?.get()?.onLoadFinished()
                    view?.get()?.onLoadFailed(it)
                    return@launch
                }
                view?.get()?.onLoadFinished()
                view?.get()?.onLoadSuccess(this@MainListMovieViewModel)
            }
        }
    }


    override fun getTypes(): PublicConfig.ContentDisplayType = PublicConfig.ContentDisplayType.MOVIE


    private suspend fun getPages(
        apiRepository: ApiRepository,
        supportedType: SupportedType,
        pages: Int,
        tag: String?
    ): ApiRepository.RetError? {
        if (pages < 0 || pages > 1000) {
            return ApiRepository.RetError(
                ErrorSectionAdapter.ERR_CODE_PARSE_FAILED,
                IllegalArgumentException("pages must between 0 and maxPages or 1000 : page $pages")
            )
        }
        apiRepository.doReqAndRetResponseAsync(
            context?.get(),
            GetMovies.getList(
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
                        val contents = arrayListOf<ResettableItem>()
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
                                contents.add(
                                    MovieAboutModel(
                                        id = getInt("id"),
                                        releaseDate = jsonCheckAndGet(get("release_date"))?.toString(),
                                        posterPath = jsonCheckAndGet(get("poster_path"))?.toString(),
                                        isAdult = getBoolean("adult"),
                                        overview = getString("overview"),
                                        genres = genres,
                                        originalTitle = getString("original_title"),
                                        originalLanguage = getString("original_language"),
                                        title = getString("title"),
                                        backdropPath = jsonCheckAndGet(get("backdrop_path"))?.toString(),
                                        popularity = getDouble("popularity"),
                                        voteCount = getInt("vote_count"),
                                        voteAverage = getDouble("vote_average"),
                                        video = getBoolean("video")
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
    enum class SupportedType : Parcelable {
        DISCOVER,
        NOW_PLAYING,
        POPULAR,
        TOP_RATED,
        UPCOMING
    }
}