package id.apwdevs.app.catalogue.view


import androidx.lifecycle.ViewModel

interface MainDetailView {
    fun onLoad()
    fun onLoadFailed(errorCode: Int, message: String?, cause: Throwable?)
    fun onLoadFinished(viewModel: ViewModel)
}