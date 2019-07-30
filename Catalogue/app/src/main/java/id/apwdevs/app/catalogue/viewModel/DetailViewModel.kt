package id.apwdevs.app.catalogue.viewModel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import id.apwdevs.app.catalogue.model.onDetail.CreditsModel
import id.apwdevs.app.catalogue.model.onDetail.ReviewResponse
import id.apwdevs.app.catalogue.model.onDetail.SocmedIDModel
import id.apwdevs.app.catalogue.repository.onDetail.DetailActivityRepository

class DetailViewModel(application: Application) : AndroidViewModel(application) {
    val id: MutableLiveData<Int> = MutableLiveData()
    val hasFirstInitialize: MutableLiveData<Boolean> = MutableLiveData()
    val hasOverlayMode: MutableLiveData<Boolean> = MutableLiveData()

    private lateinit var repository: DetailActivityRepository
    val socmedIds: LiveData<SocmedIDModel>
    val reviews: LiveData<ReviewResponse>
    val credits: LiveData<CreditsModel>
    val hasLoading: LiveData<Boolean>

    init {
        hasFirstInitialize.value = false
        hasOverlayMode.value = false
    }

    private fun setup(activity: Activity) {
        //activity.intent.getPa
    }

}