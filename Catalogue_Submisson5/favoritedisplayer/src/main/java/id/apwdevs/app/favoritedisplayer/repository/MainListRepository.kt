package id.apwdevs.app.favoritedisplayer.repository

import android.content.Context
import android.os.Parcelable
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import id.apwdevs.app.favoritedisplayer.model.FavoriteEntity
import id.apwdevs.app.favoritedisplayer.plugin.Contracts
import kotlinx.android.parcel.Parcelize

class MainListRepository(private val context: Context) {

    val favList: MutableLiveData<List<FavoriteEntity>> = MutableLiveData()

    @WorkerThread
    fun load(displayType: ContentDisplayType) {
        val list = Contracts.getFavorite(context, displayType)
        favList.postValue(list)
    }

    @Parcelize
    enum class ContentDisplayType(val type: Int) : Parcelable {
        MOVIE(0x2a),
        TV_SHOWS(0x1f);

        companion object {
            fun findId(idType: Int): ContentDisplayType? {
                values().iterator().forEach {
                    if (it.type == idType)
                        return it
                }
                return null
            }
        }
    }
}