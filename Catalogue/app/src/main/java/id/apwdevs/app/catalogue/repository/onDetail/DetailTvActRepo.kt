package id.apwdevs.app.catalogue.repository.onDetail

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import id.apwdevs.app.catalogue.activities.DetailActivity
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onDetail.OtherTVAboutModel
import id.apwdevs.app.catalogue.model.onUserMain.TvAboutModel
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer
import id.apwdevs.app.catalogue.plugin.api.GetTVShows
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

@Suppress("UNCHECKED_CAST")
class DetailTvActRepo(
    mContext: Context,
    viewModelScope: CoroutineScope
) : DetailActivityRepository(mContext, viewModelScope) {

    override val data1Obj: MutableLiveData<ResettableItem>
        get() = MutableLiveData<TvAboutModel>() as MutableLiveData<ResettableItem>
    override val data2Obj: MutableLiveData<ResettableItem>
        get() = MutableLiveData<OtherTVAboutModel>() as MutableLiveData<ResettableItem>
    override val typeContentContract: TypeContentContract
        get() = TypeContentContract.TV_SHOWS

    override fun initAtFirstTime(dataIntent: Intent) {
        dataIntent.extras?.apply {
            val otherAboutTv = getParcelable<TvAboutModel>(DetailActivity.EXTRA_CONTENT_DETAILS)
            data1Obj.postValue(otherAboutTv)
        }
    }

    override fun getDataAsync(id: Int): Deferred<Boolean> = GlobalScope.async {
        val getObjectRepo = GetObjectFromServer.getInstance(context)

        getObjectRepo.getObj(
            GetTVShows.getOtherDetails(id),
            OtherTVAboutModel::class.java,
            "GetOtherTvDetails",
            object : GetObjectFromServer.GetObjectFromServerCallback<OtherTVAboutModel> {
                override fun onSuccess(response: OtherTVAboutModel) {
                    data2Obj.postValue(response)
                }

                override fun onFailed(retError: GetObjectFromServer.RetError) {
                    hasLoading.postValue(false)
                    this@DetailTvActRepo.retError.postValue(retError)

                }

                override fun onProgress(percent: Double) {
                }

            }
        )

        true
    }
}