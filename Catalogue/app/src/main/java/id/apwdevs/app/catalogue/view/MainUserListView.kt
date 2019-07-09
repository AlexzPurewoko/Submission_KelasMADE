package id.apwdevs.app.catalogue.view

import androidx.lifecycle.ViewModel
import id.apwdevs.app.catalogue.plugin.api.ApiRepository

interface MainUserListView {
    fun onStart()
    fun onLoadFinished()
    fun onLoadSuccess(viewModel: ViewModel)
    fun onLoadFailed(err: ApiRepository.RetError)
}