package id.apwdevs.moTvCatalogue.view

import android.arch.lifecycle.ViewModel

interface MainDetailView {
    fun onStart()
    fun onLoad()
    fun onLoadFailed(errorCode: Int, message: String?, cause: Throwable?)
    fun onLoadFinished(viewModel: ViewModel)
}