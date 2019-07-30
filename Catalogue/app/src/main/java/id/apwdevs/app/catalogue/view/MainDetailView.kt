package id.apwdevs.app.catalogue.view


import id.apwdevs.app.catalogue.plugin.api.ApiRepository
import id.apwdevs.app.catalogue.viewModel.DetailViewModel

@Deprecated("Possibly to use an observer to make an interact with data", level = DeprecationLevel.ERROR)
interface MainDetailView {
    fun onLoad()
    fun onLoadFailed(err: ApiRepository.RetError)
    fun onLoadFinished(viewModel: DetailViewModel)
}