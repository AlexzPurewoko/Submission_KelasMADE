package id.apwdevs.app.catalogue.repository.onDetail

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onDetail.CreditsModel
import id.apwdevs.app.catalogue.model.onDetail.ReviewResponse
import id.apwdevs.app.catalogue.model.onDetail.SocmedIDModel
import id.apwdevs.app.catalogue.model.onUserMain.MainDataItemModel
import id.apwdevs.app.catalogue.plugin.api.GetMovies
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer
import id.apwdevs.app.catalogue.plugin.api.GetTVShows
import kotlinx.coroutines.*

@Suppress("UNCHECKED_CAST")
abstract class DetailActivityRepository protected constructor(
    protected val context: Context,
    protected val viewModelScope: CoroutineScope
) {

    val socmedIds: MutableLiveData<SocmedIDModel> = MutableLiveData()
    val reviews: MutableLiveData<ReviewResponse> = MutableLiveData()
    val credits: MutableLiveData<CreditsModel> = MutableLiveData()

    val hasLoading: MutableLiveData<Boolean> = MutableLiveData()
    val retError: MutableLiveData<GetObjectFromServer.RetError> = MutableLiveData()
    val data1Obj: MutableLiveData<ResettableItem> = MutableLiveData()
    val data2Obj: MutableLiveData<ResettableItem> = MutableLiveData()
    val loadFinished: MutableLiveData<Boolean> = MutableLiveData()
    val isFavorite: MutableLiveData<Boolean> = MutableLiveData()
    val progress: MutableLiveData<Float> = MutableLiveData()

    abstract val typeContentContract: TypeContentContract

    abstract fun getDataAsync(id: Int): Deferred<Boolean> // if no error, return into true, otherwise will be marked as error

    abstract fun initAtFirstTime(dataIntent: Intent)

    init {
        loadFinished.value = false
    }

    fun load(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            loadFinished.postValue(false)
            hasLoading.postValue(true)
            retError.postValue(null)

            val objServer = GetObjectFromServer.getInstance(context)
            updateProgress(10f)
            if (getDataAsync(id).await()) {
                updateProgress(50f)
                getCreditsAsync(id, typeContentContract, objServer).await()
                updateProgress(65f)
                getReviewsAsync(id, typeContentContract, objServer).await()
                updateProgress(80f)
                getSocmedIdAsync(id, typeContentContract, objServer).await()
                updateProgress(95f)
            }
            isFavorite.postValue(
                data1Obj.value.let {
                    if (it is MainDataItemModel)
                        it.isFavorite
                    else false
                }
            )
            updateProgress(100f)
            hasLoading.postValue(false)
            loadFinished.postValue(true)
        }
    }

    private fun updateProgress(progress: Float) {
        this.progress.postValue(progress)
    }

    private fun getCreditsAsync(
        id: Int,
        type: TypeContentContract,
        objectFromServer: GetObjectFromServer
    ) =
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

    private fun getReviewsAsync(
        id: Int,
        type: TypeContentContract,
        objectFromServer: GetObjectFromServer
    ) =
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


    private fun getSocmedIdAsync(
        id: Int,
        type: TypeContentContract,
        objectFromServer: GetObjectFromServer
    ) =
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
        if (this.retError.value == null) {
            loadFinished.postValue(true)
            this.retError.postValue(retError)
            hasLoading.postValue(false)
        }
    }

    enum class TypeContentContract {
        MOVIE,
        TV_SHOWS
    }

}