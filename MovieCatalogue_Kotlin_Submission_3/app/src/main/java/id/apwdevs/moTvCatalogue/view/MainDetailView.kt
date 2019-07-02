package id.apwdevs.moTvCatalogue.view

import android.arch.lifecycle.ViewModel

interface MainDetailView {
    fun onStart()
    fun onLoad()
    fun onLoadFailed(errorCode: Int, message: String?, exceptionName: String?)
    fun onLoadFinished(viewModel: ViewModel)
}