package id.apwdevs.app.catalogue.repository.onDetail

import android.content.Context
import androidx.lifecycle.MutableLiveData
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onDetail.CreditsModel
import id.apwdevs.app.catalogue.model.onDetail.ReviewResponse
import id.apwdevs.app.catalogue.model.onDetail.SocmedIDModel
import id.apwdevs.app.catalogue.plugin.api.GetMovies
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer
import id.apwdevs.app.catalogue.plugin.api.GetTVShows
import kotlinx.coroutines.*

@Suppress("UNCHECKED_CAST")
abstract class DetailActivityRepository protected constructor(
    protected val context: Context,
    protected val viewModelScope: CoroutineScope
) {

    val socmedIds: MutableLiveData<SocmedIDModel> = MutableLiveData()//
    val reviews: MutableLiveData<ReviewResponse> = MutableLiveData()//
    val credits: MutableLiveData<CreditsModel> = MutableLiveData()//

    val hasLoading: MutableLiveData<Boolean> = MutableLiveData()//
    //val loadSuccess: MutableLiveData<Boolean> = MutableLiveData()//
    val retError: MutableLiveData<GetObjectFromServer.RetError> = MutableLiveData()
    abstract val data1Obj: MutableLiveData<ResettableItem>
    abstract val data2Obj: MutableLiveData<ResettableItem>
    protected abstract val typeContentContract: TypeContentContract

    abstract fun getData(id: Int): Deferred<Boolean> // if no error, return into true, otherwise will be marked as error


    fun load(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            //loadSuccess.postValue(false)
            hasLoading.postValue(true)
            retError.postValue(null)

            val objServer = GetObjectFromServer.getInstance(context)
            val getDt = getData(id)

            getCredits(id, typeContentContract, objServer).await()
            getReviews(id, typeContentContract, objServer).await()
            getSocmedId(id, typeContentContract, objServer).await()

            while (getDt.isActive) {
                if (hasLoading.value != true) {
                    getDt.cancel()
                    return@launch
                }
                delay(500)
            }
            hasLoading.postValue(false)
        }
    }

    private fun getCredits(id: Int, type: TypeContentContract, objectFromServer: GetObjectFromServer) =
        GlobalScope.async {
            objectFromServer.getObj(when (type) {
                TypeContentContract.MOVIE -> GetMovies.getCredits(id)
                TypeContentContract.TV_SHOWS -> GetTVShows.getCredits(id)

            },
                CreditsModel::class.java,
                "GetCredits",
                object : GetObjectFromServer.GetObjectFromServerCallback<CreditsModel> {
                    override fun onSuccess(response: CreditsModel) {
                        credits.postValue(response)
                    }

                    override fun onFailed(retError: GetObjectFromServer.RetError) {
                        setToFailed(retError)
                    }

                    override fun onProgress(percent: Double) {
                    }

                })

        }

    private fun getReviews(id: Int, type: TypeContentContract, objectFromServer: GetObjectFromServer) =
        GlobalScope.async {
            objectFromServer.getObj(when (type) {
                TypeContentContract.MOVIE -> GetMovies.getReviews(id)
                TypeContentContract.TV_SHOWS -> GetTVShows.getReviews(id)

            },
                ReviewResponse::class.java,
                "GetReviews",
                object : GetObjectFromServer.GetObjectFromServerCallback<ReviewResponse> {
                    override fun onSuccess(response: ReviewResponse) {
                        reviews.postValue(response)
                    }

                    override fun onFailed(retError: GetObjectFromServer.RetError) {
                        setToFailed(retError)
                    }

                    override fun onProgress(percent: Double) {
                    }

                })

        }


    private fun getSocmedId(id: Int, type: TypeContentContract, objectFromServer: GetObjectFromServer) =
        GlobalScope.async {
            objectFromServer.getObj(when (type) {
                TypeContentContract.MOVIE -> GetMovies.getSocmedID(id)
                TypeContentContract.TV_SHOWS -> GetTVShows.getSocmedID(id)
            },
                SocmedIDModel::class.java,
                "GetsocmedID",
                object : GetObjectFromServer.GetObjectFromServerCallback<SocmedIDModel> {
                    override fun onSuccess(response: SocmedIDModel) {
                        socmedIds.postValue(response)
                    }

                    override fun onFailed(retError: GetObjectFromServer.RetError) {
                        setToFailed(retError)
                    }

                    override fun onProgress(percent: Double) {
                    }

                })

        }

    private fun setToFailed(retError: GetObjectFromServer.RetError) {
        //loadSuccess.postValue(false)
        this.retError.postValue(retError)
        hasLoading.postValue(false)
    }

    protected enum class TypeContentContract {
        MOVIE,
        TV_SHOWS
    }

}