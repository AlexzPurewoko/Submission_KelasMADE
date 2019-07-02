package id.apwdevs.moTvCatalogue.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import id.apwdevs.moTvCatalogue.model.OtherAboutFilmModel
import id.apwdevs.moTvCatalogue.model.onDetail.CreditsModel
import id.apwdevs.moTvCatalogue.model.onDetail.OtherMovieAboutModel
import id.apwdevs.moTvCatalogue.model.onDetail.ReviewModel
import id.apwdevs.moTvCatalogue.model.onDetail.SocmedIDModel
import id.apwdevs.moTvCatalogue.plugin.ApiRepository
import id.apwdevs.moTvCatalogue.plugin.PublicConfig
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONObject

class DetailMovieViewModel : ViewModel() {
    val shortListDetais: MutableLiveData<OtherAboutFilmModel> = MutableLiveData()
    val listOtherDetails: MutableLiveData<OtherMovieAboutModel> = MutableLiveData()
    val socmedIds: MutableLiveData<SocmedIDModel> = MutableLiveData()
    val reviews: MutableLiveData<ReviewModel> = MutableLiveData()
    val credits: MutableLiveData<CreditsModel> = MutableLiveData()


    fun setListSocmedId(apiRepository: ApiRepository, idMovies: Int): Deferred<ANError?> = GlobalScope.async {

        val getRequests = apiRepository.doRequestAndReturnJSON(
            "${PublicConfig.URL_API}/${PublicConfig.MOVIE_DIR_PATH}/301528/${PublicConfig.EXTERNAL_ID_QNAME}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}",
            "SocmedID",
            Priority.HIGH
        ).await()

        getRequests?.let {
            if (it.isSuccess && !it.response.isNullOrEmpty()) {
                JSONObject(it.response).apply {
                    socmedIds.postValue(
                        SocmedIDModel(
                            id = getInt("id"),
                            facebookId = getString("facebook_id"),
                            instagramId = getString("instagram_id"),
                            twitterId = getString("twitter_id")
                        )
                    )
                }
                return@async null
            } else {
                return@async it.anErrorIfAny
            }
        }

    }

    override fun onCleared() {

    }
}