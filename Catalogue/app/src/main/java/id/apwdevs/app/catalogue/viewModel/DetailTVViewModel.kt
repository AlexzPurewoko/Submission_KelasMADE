package id.apwdevs.app.catalogue.viewModel

import androidx.lifecycle.MutableLiveData
import com.androidnetworking.common.Priority
import id.apwdevs.app.catalogue.activities.DetailActivity
import id.apwdevs.app.catalogue.model.onDetail.ModelTvCreatedBy
import id.apwdevs.app.catalogue.model.onDetail.OtherTVAboutModel
import id.apwdevs.app.catalogue.model.onDetail.ProductionTVCompaniesModel
import id.apwdevs.app.catalogue.model.onDetail.ProductionTVSeasons
import id.apwdevs.app.catalogue.model.onUserMain.TvAboutModel
import id.apwdevs.app.catalogue.plugin.CoroutineContextProvider
import id.apwdevs.app.catalogue.plugin.PublicConfig
import id.apwdevs.app.catalogue.plugin.api.ApiRepository
import id.apwdevs.app.catalogue.plugin.api.GetTVShows
import id.apwdevs.app.catalogue.plugin.jsonCheckAndGet
import id.apwdevs.app.catalogue.plugin.view.ErrorSectionAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class DetailTVViewModel : DetailViewModel() {

    val shortDetails: MutableLiveData<TvAboutModel> = MutableLiveData()
    val otherDetails: MutableLiveData<OtherTVAboutModel> = MutableLiveData()

    override fun getAll(
        coroutineContextProvider: CoroutineContextProvider
    ) {
        GlobalScope.launch(coroutineContextProvider.main) {
            view?.get()?.onLoad()
            hasLoading.value = true
            loadSuccess.value = false
            activity?.get()?.intent?.extras?.apply {
                val otherAboutTv = getParcelable<TvAboutModel>(DetailActivity.EXTRA_CONTENT_DETAILS)
                shortDetails.postValue(otherAboutTv)
                id.value = getInt(DetailActivity.EXTRA_ID)
            }

            val idTv = id.value ?: 0

            getCredits(apiRepository)?.let {
                view?.get()?.onLoadFailed(it)
                return@launch
            }
            otherDetails(apiRepository, idTv)?.let {
                view?.get()?.onLoadFailed(it)
                return@launch
            }
            getReviews(apiRepository)?.let {
                view?.get()?.onLoadFailed(it)
                return@launch
            }
            getSocmedId(apiRepository)?.let {
                view?.get()?.onLoadFailed(it)
                return@launch
            }
            hasLoading.value = false
            loadSuccess.value = true
            view?.get()?.onLoadFinished(this@DetailTVViewModel)
        }
    }


    override fun getTypes(): PublicConfig.ContentDisplayType = PublicConfig.ContentDisplayType.TV_SHOWS

    private suspend fun otherDetails(apiRepository: ApiRepository, idTv: Int): ApiRepository.RetError? =
        apiRepository.doReqAndRetResponseAsync(
            activity?.get(),
            GetTVShows.getOtherDetails(idTv),
            "getTvOtherDetailsId$idTv",
            Priority.HIGH
        ).await()?.let {
            return if (it.isSuccess && !it.response.isNullOrEmpty()) {
                try {
                    JSONObject(it.response).apply {
                        val resultProductionCompaniesModel = mutableListOf<ProductionTVCompaniesModel>()
                        val resultProductionSeasonModel = mutableListOf<ProductionTVSeasons>()
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
                        for (index in 0 until jsonCreatedBy.length()) {
                            jsonCreatedBy.getJSONObject(index).apply {
                                resultCreatedBy.add(
                                    ModelTvCreatedBy(
                                        creditId = getString("profile_path"),
                                        id = getInt("id"),
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
                                productionCompanies = resultProductionCompaniesModel,
                                productionTvSeasons = resultProductionSeasonModel
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
}