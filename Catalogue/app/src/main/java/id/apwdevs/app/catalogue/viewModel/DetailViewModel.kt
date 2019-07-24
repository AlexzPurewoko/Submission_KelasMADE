package id.apwdevs.app.catalogue.viewModel

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.common.Priority
import id.apwdevs.app.catalogue.model.onDetail.*
import id.apwdevs.app.catalogue.plugin.CoroutineContextProvider
import id.apwdevs.app.catalogue.plugin.PublicConfig
import id.apwdevs.app.catalogue.plugin.api.ApiRepository
import id.apwdevs.app.catalogue.plugin.api.GetMovies
import id.apwdevs.app.catalogue.plugin.api.GetTVShows
import id.apwdevs.app.catalogue.plugin.jsonCheckAndGet
import id.apwdevs.app.catalogue.plugin.view.ErrorSectionAdapter
import id.apwdevs.app.catalogue.view.MainDetailView
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference

abstract class DetailViewModel : ViewModel() {
    val socmedIds: MutableLiveData<SocmedIDModel> = MutableLiveData()
    val reviews: MutableLiveData<ReviewModel> = MutableLiveData()
    val credits: MutableLiveData<CreditsModel> = MutableLiveData()
    val id: MutableLiveData<Int> = MutableLiveData()

    val hasFirstInitialize: MutableLiveData<Boolean> = MutableLiveData()
    val hasLoading: MutableLiveData<Boolean> = MutableLiveData()
    val loadSuccess: MutableLiveData<Boolean> = MutableLiveData()
    val hasOverlayMode: MutableLiveData<Boolean> = MutableLiveData()

    var activity: WeakReference<Activity>? = null
    var view: WeakReference<MainDetailView>? = null
    val apiRepository = ApiRepository()

    init {
        hasFirstInitialize.value = false
        hasLoading.value = false
        loadSuccess.value = false
        hasOverlayMode.value = false
    }

    fun setup(activity: Activity, view: MainDetailView) {
        this.activity = WeakReference(activity)
        this.view = WeakReference(view)
    }

    protected abstract fun getTypes(): PublicConfig.ContentDisplayType

    abstract fun getAll(coroutineContextProvider: CoroutineContextProvider = CoroutineContextProvider())

    suspend fun getCredits(apiRepository: ApiRepository): ApiRepository.RetError? =
        when (getTypes()) {
            PublicConfig.ContentDisplayType.MOVIE ->
                apiRepository.doReqAndRetResponseAsync(
                    activity?.get(),
                    GetMovies.getCredits(requireNotNull(id.value)),
                    "getCreditsMoviesId${id.value}",
                    Priority.HIGH
                )
            PublicConfig.ContentDisplayType.TV_SHOWS ->
                apiRepository.doReqAndRetResponseAsync(
                    activity?.get(),
                    GetTVShows.getCredits(requireNotNull(id.value)),
                    "getCreditsTvId${id.value}",
                    Priority.HIGH
                )
        }.await()?.let {
            return if (it.isSuccess && !it.response.isNullOrEmpty()) {
                try {
                    JSONObject(it.response).apply {
                        val resultCasts = arrayListOf<CastModel>()
                        val resultCrews = arrayListOf<CrewModel>()
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
                                        castId =
                                        when (getTypes()) {
                                            PublicConfig.ContentDisplayType.MOVIE -> getInt("cast_id")
                                            PublicConfig.ContentDisplayType.TV_SHOWS -> null
                                        },
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

                        credits.value =
                            CreditsModel(
                                id = getInt("id"),
                                allCasts = resultCasts,
                                allCrew = resultCrews
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

    suspend fun getReviews(apiRepository: ApiRepository): ApiRepository.RetError? =
        when (getTypes()) {
            PublicConfig.ContentDisplayType.MOVIE ->
                apiRepository.doReqAndRetResponseAsync(
                    activity?.get(),
                    GetMovies.getReviews(requireNotNull(id.value)),
                    "getReviewsMoviesId${id.value}",
                    Priority.HIGH
                )
            PublicConfig.ContentDisplayType.TV_SHOWS ->
                apiRepository.doReqAndRetResponseAsync(
                    activity?.get(),
                    GetTVShows.getReviews(requireNotNull(id.value)),
                    "getReviewsTvId${id.value}",
                    Priority.HIGH
                )
        }.await()?.let {
            return if (it.isSuccess && !it.response.isNullOrEmpty()) {
                try {
                    JSONObject(it.response).apply {

                        val ret = getJSONArray("results")
                        val results = arrayListOf<ReviewResultModel>()
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

                        reviews.value =
                            ReviewModel(
                                id = getInt("id"),
                                page = getInt("page"),
                                totalPages = getInt("total_pages"),
                                totalResult = getInt("total_results"),
                                results = results
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

    suspend fun getSocmedId(apiRepository: ApiRepository): ApiRepository.RetError? =
        when (getTypes()) {
            PublicConfig.ContentDisplayType.MOVIE ->
                apiRepository.doReqAndRetResponseAsync(
                    activity?.get(),
                    GetMovies.getSocmedID(requireNotNull(id.value)),
                    "getSocmedMoviesId${id.value}",
                    Priority.HIGH
                )
            PublicConfig.ContentDisplayType.TV_SHOWS ->
                apiRepository.doReqAndRetResponseAsync(
                    activity?.get(),
                    GetTVShows.getSocmedID(requireNotNull(id.value)),
                    "getSocmedTvId${id.value}",
                    Priority.HIGH
                )
        }.await()?.let {
            return if (it.isSuccess && !it.response.isNullOrEmpty()) {
                JSONObject(it.response).apply {
                    socmedIds.value =
                        SocmedIDModel(
                            id = getInt("id"),
                            facebookId = jsonCheckAndGet(get("facebook_id"))?.toString(),
                            instagramId = jsonCheckAndGet(get("instagram_id"))?.toString(),
                            twitterId = jsonCheckAndGet(get("twitter_id"))?.toString()
                        )
                }
                null
            } else {
                it.anErrorIfAny
            }
        }
}