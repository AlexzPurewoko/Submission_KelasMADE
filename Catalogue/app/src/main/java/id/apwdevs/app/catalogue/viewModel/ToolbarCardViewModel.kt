package id.apwdevs.app.catalogue.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.apwdevs.app.catalogue.plugin.PublicConfig

class ToolbarCardViewModel : ViewModel() {
    val currentListMode: MutableLiveData<Int> = MutableLiveData()
    val isInSearchMode: MutableLiveData<Boolean> = MutableLiveData()
    val queryTextSearch: MutableLiveData<QueryStrData> = MutableLiveData()

    init {
        currentListMode.value = PublicConfig.RecyclerMode.MODE_LIST
        isInSearchMode.value = false
    }

    data class QueryStrData(
        val s: CharSequence?, val start: Int, val before: Int, val count: Int
    )
}