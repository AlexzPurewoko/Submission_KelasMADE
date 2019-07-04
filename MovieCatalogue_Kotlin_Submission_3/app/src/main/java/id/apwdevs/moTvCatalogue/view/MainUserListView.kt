package id.apwdevs.moTvCatalogue.view

import  android.arch.lifecycle.ViewModel

interface MainUserListView {
    fun onStart()
    fun onLoadFinished()
    fun onLoadSuccess(viewModel: ViewModel)
    fun onLoadFailed(errorCode: Int, message: String?, cause: Throwable?)
}