package id.apwdevs.app.catalogue.viewModel

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.common.Priority
import id.apwdevs.app.catalogue.model.onDetail.*
import id.apwdevs.app.catalogue.model.onUserMain.MovieAboutModel
import id.apwdevs.app.catalogue.plugin.CoroutineContextProvider
import id.apwdevs.app.catalogue.plugin.api.ApiRepository
import id.apwdevs.app.catalogue.plugin.api.GetMovies
import id.apwdevs.app.catalogue.plugin.jsonCheckAndGet
import id.apwdevs.app.catalogue.plugin.view.ErrorSectionAdapter
import id.apwdevs.app.catalogue.view.MainDetailView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class DetailMovieViewModel : ViewModel() {
    val shortListDetais: MutableLiveData<MovieAboutModel> = MutableLiveData()
    val listOtherDetails: MutableLiveData<OtherMovieAboutModel> = MutableLiveData()
    val socmedIds: MutableLiveData<SocmedIDModel> = MutableLiveData()
    val reviews: MutableLiveData<ReviewModel> = MutableLiveData()
    val credits: MutableLiveData<CreditsModel> = MutableLiveData()

    val hasFirstInitialize: MutableLiveData<Boolean> = MutableLiveData()

    init {
        hasFirstInitialize.value = false
    }

    fun setAll(
        activity: Activity,
        apiRepository: ApiRepository,
        idMovies: Int,
        view: MainDetailView,
        coroutineContextProvider: CoroutineContextProvider = CoroutineContextProvider()
    ) {
        GlobalScope.launch(coroutineContextProvider.main) {
            view.onLoad()
            activity.intent?.apply {
                val otherAboutFilmModel = getParcelableExtra<MovieAboutModel>("MOVIE_DETAILS")
                shortListDetais.postValue(otherAboutFilmModel)
            }

            setCredits(apiRepository, idMovies)?.let {
                view.onLoadFailed(it.errorCode, it.cause?.message, it.cause)
                return@launch
            }
            otherDetails(apiRepository, idMovies)?.let {
                view.onLoadFailed(it.errorCode, it.cause?.message, it.cause)
                return@launch
            }
            setReviews(apiRepository, idMovies)?.let {
                view.onLoadFailed(it.errorCode, it.cause?.message, it.cause)
                return@launch
            }
            setListSocmedId(apiRepository, idMovies)?.let {
                view.onLoadFailed(it.errorCode, it.cause?.message, it.cause)
                return@launch
            }

            view.onLoadFinished(this@DetailMovieViewModel)
        }
    }

    suspend fun otherDetails(apiRepository: ApiRepository, idMovies: Int): ApiRepository.RetError? =
        apiRepository.doReqAndRetResponseAsync(
            GetMovies.getOtherDetails(idMovies),
            "getOtherDetails$idMovies",
            Priority.HIGH
        ).await()?.let {
            return if (it.isSuccess && !it.response.isNullOrEmpty()) {
                try {
                    JSONObject(it.response).apply {
                        val resultProductionCompaniesModel = mutableListOf<ProductionCompaniesModel>()
                        val resultProductionCountryModel = mutableListOf<ProductionCountryModel>()
                        val jsonProdComp = getJSONArray("production_companies")
                        val jsonProdCountry = getJSONArray("production_countries")
                        for (index in 0 until jsonProdComp.length()) {
                            jsonProdComp.getJSONObject(index).apply {
                                resultProductionCompaniesModel.add(
                                    ProductionCompaniesModel(
                                        logoPath = getString("logo_path"),
                                        id = getInt("id"),
                                        originCountry = getString("origin_country"),
                                        name = getString("name")
                                    )
                                )
                            }
                        }

                        for (index in 0 until jsonProdCountry.length()) {
                            jsonProdCountry.getJSONObject(index).apply {
                                resultProductionCountryModel.add(
                                    ProductionCountryModel(
                                        stringName = getString("name"),
                                        iso31661 = getString("iso_3166_1")
                                    )
                                )
                            }
                        }

                        val homepage = jsonCheckAndGet(get("homepage"))?.toString()
                        val tagLine = jsonCheckAndGet(get("tagline"))?.toString()
                        val runtime = jsonCheckAndGet(get("runtime"))?.toString()?.toInt()
                        listOtherDetails.postValue(
                            OtherMovieAboutModel(
                                movieBudget = getInt("budget"),
                                homepage = homepage,
                                revenue = getInt("revenue"),
                                runtime = runtime,
                                status = getString("status"),
                                tagLine = tagLine,
                                productionCompanies = resultProductionCompaniesModel,
                                productionCountry = resultProductionCountryModel
                            )
                        )
                    }
                } catch (e: JSONException) {
                    return ApiRepository.RetError(ErrorSectionAdapter.ERR_CODE_PARSE_FAILED, e)
                }
                null
            } else {
                it.anErrorIfAny
            }

        }


    suspend fun setCredits(apiRepository: ApiRepository, idMovies: Int): ApiRepository.RetError? =
        apiRepository.doReqAndRetResponseAsync(
            GetMovies.getCredits(idMovies),
            "getCreditsMoviesId$idMovies",
            Priority.HIGH
        ).await()?.let {
            return if (it.isSuccess && !it.response.isNullOrEmpty()) {
                try {
                    JSONObject(it.response).apply {
                        val resultCasts = mutableListOf<CastModel>()
                        val resultCrews = mutableListOf<CrewModel>()
                        val jsonCrews = getJSONArray("crew")
                        val jsonCast = getJSONArray("cast")
                        for (index in 0 until jsonCrews.length()) {
                            jsonCrews.getJSONObject(index).apply {
                                resultCrews.add(
                                    CrewModel(
                                        creditId = getString("credit_id"),
                                        department = getString("department"),
                                        gender = jsonCheckAndGet(get("gender"))?.toString()?.toInt(),
                                        id = getInt("id"),
                                        job = getString("job"),
                                        name = getString("name"),
                                        profilePath = jsonCheckAndGet(get("profile_path"))?.toString()
                                    )
                                )
                            }
                        }

                        for (index in 0 until jsonCast.length()) {
                            jsonCast.getJSONObject(index).apply {
                                resultCasts.add(
                                    CastModel(
                                        id = getInt("id"),
                                        castId = getInt("cast_id"),
                                        asCharacter = getString("character"),
                                        creditId = getString("credit_id"),
                                        gender = jsonCheckAndGet(get("gender"))?.toString()?.toInt(),
                                        name = getString("name"),
                                        order = getInt("order"),
                                        profilePath = jsonCheckAndGet(get("profile_path"))?.toString()
                                    )
                                )
                            }
                        }

                        credits.postValue(
                            CreditsModel(
                                id = getInt("id"),
                                allCasts = resultCasts,
                                allCrew = resultCrews
                            )
                        )
                    }
                } catch (e: JSONException) {
                    return ApiRepository.RetError(ErrorSectionAdapter.ERR_CODE_PARSE_FAILED, e)
                }
                null
            } else {
                it.anErrorIfAny
            }
        }

    suspend fun setReviews(apiRepository: ApiRepository, idMovies: Int): ApiRepository.RetError? =
        apiRepository.doReqAndRetResponseAsync(
            GetMovies.getReviews(idMovies),
            "getReviewsMoviesId$idMovies",
            Priority.HIGH
        ).await()?.let {
            return if (it.isSuccess && !it.response.isNullOrEmpty()) {
                try {
                    JSONObject(it.response).apply {

                        val ret = getJSONArray("results")
                        val results = mutableListOf<ReviewResultModel>()
                        for (index in 0 until ret.length()) {
                            ret.getJSONObject(index).apply {
                                results.add(
                                    ReviewResultModel(
                                        id = getString("id"),
                                        author = getString("author"),
                                        content = getString("content"),
                                        url = getString("url")
                                    )
                                )
                            }
                        }

                        reviews.postValue(
                            ReviewModel(
                                id = getInt("id"),
                                page = getInt("page"),
                                totalPages = getInt("total_pages"),
                                totalResult = getInt("total_results"),
                                results = results
                            )
                        )
                    }
                } catch (e: JSONException) {
                    return ApiRepository.RetError(ErrorSectionAdapter.ERR_CODE_PARSE_FAILED, e)
                }
                null
            } else {
                it.anErrorIfAny
            }
        }

    suspend fun setListSocmedId(apiRepository: ApiRepository, idMovies: Int): ApiRepository.RetError? =
        apiRepository.doReqAndRetResponseAsync(
            GetMovies.getSocmedID(idMovies),
            "getSocmedMoviesId$idMovies",
            Priority.HIGH
        ).await()?.let {
            return if (it.isSuccess && !it.response.isNullOrEmpty()) {
                JSONObject(it.response).apply {
                    socmedIds.postValue(
                        SocmedIDModel(
                            id = getInt("id"),
                            facebookId = jsonCheckAndGet(get("facebook_id"))?.toString(),
                            instagramId = jsonCheckAndGet(get("instagram_id"))?.toString(),
                            twitterId = jsonCheckAndGet(get("twitter_id"))?.toString()
                        )
                    )
                }
                null
            } else {
                it.anErrorIfAny
            }
        }


    override fun onCleared() {

    }
}