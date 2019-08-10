package id.apwdevs.app.favoritedisplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import id.apwdevs.app.favoritedisplayer.model.FavoriteEntity
import id.apwdevs.app.favoritedisplayer.repository.MainListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainListViewModel(application: Application) : AndroidViewModel(application) {

    var mFavList: MutableLiveData<List<FavoriteEntity>>
    val hasLoading: MutableLiveData<Boolean> = MutableLiveData()
    val hasFirstInstantiate: MutableLiveData<Boolean> = MutableLiveData()

    var type: MainListRepository.ContentDisplayType? = null
    private var mainListRepository: MainListRepository = MainListRepository(getApplication())

    init {
        mFavList = mainListRepository.favList
        hasFirstInstantiate.value = false
        hasLoading.value = false
    }


    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            hasLoading.postValue(true)
            type?.let {
                mainListRepository.load(it)
            }
            hasLoading.postValue(false)
        }
    }
}