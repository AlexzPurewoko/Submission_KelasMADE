package id.apwdevs.app.catalogue.repository.onDetail

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import id.apwdevs.app.catalogue.activities.DetailActivity
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onDetail.OtherMovieAboutModel
import id.apwdevs.app.catalogue.model.onUserMain.MovieAboutModel
import id.apwdevs.app.catalogue.plugin.api.GetMovies
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

@Suppress("UNCHECKED_CAST")
class DetailMovieActRepo(
    mContext: Context,
    viewModelScope: CoroutineScope
) : DetailActivityRepository(mContext, viewModelScope) {

    override val data1Obj: MutableLiveData<ResettableItem>
        get() = MutableLiveData<MovieAboutModel>() as MutableLiveData<ResettableItem>
    override val data2Obj: MutableLiveData<ResettableItem>
        get() = MutableLiveData<OtherMovieAboutModel>() as MutableLiveData<ResettableItem>
    override val typeContentContract: TypeContentContract
        get() = TypeContentContract.MOVIE

    override fun initAtFirstTime(dataIntent: Intent) {
        dataIntent.extras?.apply {
            val otherAboutTv = getParcelable<MovieAboutModel>(DetailActivity.EXTRA_CONTENT_DETAILS)
            data1Obj.postValue(otherAboutTv)
        }
    }

    override fun getDataAsync(id: Int): Deferred<Boolean> = GlobalScope.async {
        val getObjectRepo = GetObjectFromServer.getInstance(context)

        getObjectRepo.getObj(
            GetMovies.getOtherDetails(id),
            OtherMovieAboutModel::class.java,
            "GetOtherMovieDetails",
            object : GetObjectFromServer.GetObjectFromServerCallback<OtherMovieAboutModel> {
                override fun onSuccess(response: OtherMovieAboutModel) {
                    data2Obj.postValue(response)
                }

                override fun onFailed(retError: GetObjectFromServer.RetError) {
                    hasLoading.postValue(false)
                    this@DetailMovieActRepo.retError.postValue(retError)
                }

                override fun onProgress(percent: Double) {
                }

            }
        )

        true
    }
}