package id.apwdevs.moTvCatalogue.viewModel

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import id.apwdevs.moTvCatalogue.model.onDetail.*
import id.apwdevs.moTvCatalogue.model.onUserMain.TvAboutModel
import id.apwdevs.moTvCatalogue.plugin.CoroutineContextProvider
import id.apwdevs.moTvCatalogue.plugin.api.ApiRepository
import id.apwdevs.moTvCatalogue.plugin.api.GetTVShows
import id.apwdevs.moTvCatalogue.plugin.jsonCheckAndGet
import id.apwdevs.moTvCatalogue.view.MainDetailView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class DetailTVViewModel : ViewModel() {
    val shortDetails: MutableLiveData<TvAboutModel> = MutableLiveData()
    val otherDetails: MutableLiveData<OtherTVAboutModel> = MutableLiveData()
    val socmedIds: MutableLiveData<SocmedIDModel> = MutableLiveData()
    val reviews: MutableLiveData<ReviewModel> = MutableLiveData()
    val credits: MutableLiveData<CreditsModel> = MutableLiveData()

    fun setAll(
        activity: Activity,
        apiRepository: ApiRepository,
        idMovies: Int,
        view: MainDetailView,
        coroutineContextProvider: CoroutineContextProvider = CoroutineContextProvider()
    ) {
        view.onStart()
        GlobalScope.launch(coroutineContextProvider.main) {
            view.onLoad()
            activity.intent?.apply {
                val otherAboutTv = getParcelableExtra<TvAboutModel>("TV_DETAILS")
                shortDetails.postValue(otherAboutTv)
            }

            getCredits(apiRepository, idMovies)?.let {
                view.onLoadFailed(it.errorCode, it.errorBody, it.cause)
                return@launch
            }
            otherDetails(apiRepository, idMovies)?.let {
                view.onLoadFailed(it.errorCode, it.errorBody, it.cause)
                return@launch
            }
            getReviews(apiRepository, idMovies)?.let {
                view.onLoadFailed(it.errorCode, it.errorBody, it.cause)
                return@launch
            }
            getSocmedID(apiRepository, idMovies)?.let {
                view.onLoadFailed(it.errorCode, it.errorBody, it.cause)
                return@launch
            }

            view.onLoadFinished(this@DetailTVViewModel)
        }
    }

    suspend fun otherDetails(apiRepository: ApiRepository, idTv: Int): ANError? =
        apiRepository.doRequestAndReturnJSON(
            GetTVShows.getOtherDetails(idTv),
            "getTvOtherDetailsId$idTv",
            Priority.HIGH
        ).await()?.let {
            return if (it.isSuccess && !it.response.isNullOrEmpty()) {
                try {
                    JSONObject(it.response).apply {
                        val resultProductionCompaniesModel = mutableListOf<ProductionTVCompaniesModel>()
                        val resultProductionSeasonModel = mutableListOf<ProductionTVSeasons>()
                        val resultNetworks = mutableListOf<TvNetworkModel>()
                        val resultCreatedBy = mutableListOf<ModelTvCreatedBy>()
                        val jsonProdComp = getJSONArray("production_companies")
                        val jsonProdSeasons = getJSONArray("seasons")
                        val jsonCreatedBy = getJSONArray("created_by")
                        val jsonNetworks = getJSONArray("networks")
                        for (index in 0 until jsonProdComp.length()) {
                            jsonProdComp.getJSONObject(index).apply {
                                resultProductionCompaniesModel.add(
                                    ProductionTVCompaniesModel(
                                        logoPath = jsonCheckAndGet(get("logo_path"))?.toString(),
                                        id = getInt("id"),
                                        originCountry = getString("origin_country"),
                                        name = getString("name")
                                    )
                                )
                            }
                        }
                        for (index in 0 until jsonProdSeasons.length()) {
                            jsonProdSeasons.getJSONObject(index).apply {
                                resultProductionSeasonModel.add(
                                    ProductionTVSeasons(
                                        airDate = getString("air_date"),
                                        episodeCount = getInt("episode_count"),
                                        id = getInt("id"),
                                        name = getString("name"),
                                        overview = getString("overview"),
                                        posterPath = getString("poster_path"),
                                        seasonNumber = getInt("season_number")
                                    )
                                )
                            }
                        }
                        for (index in 0 until jsonNetworks.length()) {
                            jsonNetworks.getJSONObject(index).apply {
                                resultNetworks.add(
                                    TvNetworkModel(
                                        logoPath = getString("logo_path"),
                                        id = getInt("id"),
                                        originCountry = getString("origin_country"),
                                        name = getString("name")
                                    )
                                )
                            }
                        }
                        for (index in 0 until jsonCreatedBy.length()) {
                            jsonCreatedBy.getJSONObject(index).apply {
                                resultCreatedBy.add(
                                    ModelTvCreatedBy(
                                        creditId = getString("profile_path"),
                                        id = getInt("id"),
                                        gender = getInt("gender"),
                                        name = getString("name"),
                                        profilePath = getString("profile_path")
                                    )
                                )
                            }
                        }
                        val originCountry = mutableListOf<String>()
                        val jsonOriginCountry = getJSONArray("origin_country")
                        for (index in 0 until jsonOriginCountry.length()) {
                            originCountry.add(jsonOriginCountry.getString(index))
                        }
                        otherDetails.postValue(
                            OtherTVAboutModel(
                                createdBy = resultCreatedBy,
                                homepage = getString("homepage"),
                                firstAirDate = getString("first_air_date"),
                                inProduction = getBoolean("in_production"),
                                lastAirDate = getString("last_air_date"),
                                numberOfEpisodes = getInt("number_of_episodes"),
                                numberOfSeasons = getInt("number_of_seasons"),
                                originCountry = originCountry,
                                status = getString("status"),
                                type = getString("type"),
                                networks = resultNetworks,
                                productionCompanies = resultProductionCompaniesModel,
                                productionTvSeasons = resultProductionSeasonModel
                            )
                        )
                    }
                } catch (e: JSONException) {
                    return ANError(e)
                }
                null
            } else {
                it.anErrorIfAny
            }

        }


    suspend fun getCredits(apiRepository: ApiRepository, idTv: Int): ANError? =
        apiRepository.doRequestAndReturnJSON(
            GetTVShows.getCredits(idTv),
            "getCreditsMoviesId$idTv",
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
                                        castId = null,
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
                    return ANError(e)
                }
                null
            } else {
                it.anErrorIfAny
            }
        }

    suspend fun getReviews(apiRepository: ApiRepository, idTv: Int): ANError? =
        apiRepository.doRequestAndReturnJSON(
            GetTVShows.getReviews(idTv),
            "getReviewsTVId$idTv",
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
                    return ANError(e)
                }
                null
            } else {
                it.anErrorIfAny
            }
        }

    suspend fun getSocmedID(apiRepository: ApiRepository, idTv: Int): ANError? =
        apiRepository.doRequestAndReturnJSON(
            GetTVShows.getSocmedID(idTv),
            "getSocmedTVId$idTv",
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