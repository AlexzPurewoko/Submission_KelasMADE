package id.apwdevs.app.catalogue.viewModel

import androidx.lifecycle.MutableLiveData
import com.androidnetworking.common.Priority
import id.apwdevs.app.catalogue.activities.DetailActivity
import id.apwdevs.app.catalogue.model.onDetail.OtherMovieAboutModel
import id.apwdevs.app.catalogue.model.onDetail.ProductionCompaniesModel
import id.apwdevs.app.catalogue.model.onDetail.ProductionCountryModel
import id.apwdevs.app.catalogue.model.onUserMain.MovieAboutModel
import id.apwdevs.app.catalogue.plugin.CoroutineContextProvider
import id.apwdevs.app.catalogue.plugin.PublicConfig
import id.apwdevs.app.catalogue.plugin.api.ApiRepository
import id.apwdevs.app.catalogue.plugin.api.GetMovies
import id.apwdevs.app.catalogue.plugin.jsonCheckAndGet
import id.apwdevs.app.catalogue.plugin.view.ErrorSectionAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class DetailMovieViewModel : DetailViewModel() {

    val details: MutableLiveData<MovieAboutModel> = MutableLiveData()
    val otherDetails: MutableLiveData<OtherMovieAboutModel> = MutableLiveData()

    override fun getAll(coroutineContextProvider: CoroutineContextProvider) {
        GlobalScope.launch(coroutineContextProvider.main) {
            view?.get()?.onLoad()
            hasLoading.value = true
            loadSuccess.value = false
            activity?.get()?.intent?.extras?.apply {
                val otherAboutFilmModel = getParcelable<MovieAboutModel>(DetailActivity.EXTRA_CONTENT_DETAILS)
                details.postValue(otherAboutFilmModel)
                id.value = getInt(DetailActivity.EXTRA_ID)
            }

            val idMovies = id.value ?: 0

            getCredits(apiRepository)?.let {
                view?.get()?.onLoadFailed(it)
                return@launch
            }
            otherDetails(apiRepository, idMovies)?.let {
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
            view?.get()?.onLoadFinished(this@DetailMovieViewModel)
        }
    }

    override fun getTypes(): PublicConfig.ContentDisplayType = PublicConfig.ContentDisplayType.MOVIE

    private suspend fun otherDetails(apiRepository: ApiRepository, idMovies: Int): ApiRepository.RetError? =
        apiRepository.doReqAndRetResponseAsync(
            activity?.get(),
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
                        otherDetails.value =
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
