package id.apwdevs.app.catalogue.view


import androidx.lifecycle.ViewModel
import id.apwdevs.app.catalogue.plugin.api.ApiRepository

interface MainDetailView {
    fun onLoad()
    fun onLoadFailed(err: ApiRepository.RetError)
    fun onLoadFinished(viewModel: ViewModel)
}