package id.apwdevs.app.catalogue.viewModel

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Parcelable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import id.apwdevs.app.catalogue.model.ClassResponse
import id.apwdevs.app.catalogue.model.GenreModel
import id.apwdevs.app.catalogue.model.onUserMain.MainDataItemResponse
import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.plugin.api.GetImageFiles
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer
import id.apwdevs.app.catalogue.repository.onUserMain.FragmentContentRepository
import kotlinx.android.parcel.Parcelize

@Suppress("UNCHECKED_CAST")
class MainListViewModel(application: Application) : AndroidViewModel(application) {
    var backdropSize: String = GetImageFiles.LIST_SUPPORTED_WSIZES[3].toString()
    private var cardBgStatus: Boolean = true
    private var cardBgMode: String = "bg_darken"
    var colorredTextState: Boolean = true
    var cardItemBg: ItemCardOptions = ItemCardOptions.LIGHT

    val hasFirstInstantiate: MutableLiveData<Boolean> = MutableLiveData()
    val prevListMode: MutableLiveData<Int> = MutableLiveData()
    val hasForceLoadContent: MutableLiveData<Boolean> = MutableLiveData()

    private var repository: FragmentContentRepository<ClassResponse>? = null

    var objData: LiveData<ClassResponse>? = null
    var isLoading: LiveData<Boolean>? = null
    private var allGenre: LiveData<List<GenreModel>>? = null
    var retError: LiveData<GetObjectFromServer.RetError>? = null

    init {
        // we have to initialize these variables to be available for first instance
        hasFirstInstantiate.value = false
        prevListMode.value = 0
        hasForceLoadContent.value = false
    }

    fun setup(
        type: PublicContract.ContentDisplayType
    ) {
        repository = when (type) {
            PublicContract.ContentDisplayType.MOVIE, PublicContract.ContentDisplayType.TV_SHOWS ->
                FragmentContentRepository<MainDataItemResponse>(
                    getApplication(),
                    type,
                    viewModelScope
                ) as FragmentContentRepository<ClassResponse>
            PublicContract.ContentDisplayType.FAVORITES ->
                FragmentContentRepository(
                    getApplication(),
                    PublicContract.ContentDisplayType.FAVORITES,
                    viewModelScope
                )

        }
        isLoading = repository?.isLoading
        allGenre = repository?.allGenre
        objData = repository?.objData
        retError = repository?.retError

    }

    fun getAt(
        types: Parcelable, // get as MovieTypeContract or TvTypeContract
        pages: Int
    ) {
        repository?.load(types)
    }

    fun applyConfiguration() {
        val ctx: Context = getApplication()
        ctx.getSharedPreferences(PublicContract.SHARED_PREF_GLOBAL_NAME, Context.MODE_PRIVATE).apply {
            cardBgStatus = getBoolean("card_bg_status", cardBgStatus)
            colorredTextState = getBoolean("colored_text_state", colorredTextState)
            backdropSize = getString("carddrop_w_key", backdropSize) ?: backdropSize
            cardBgMode = getString("card_bg_mode", cardBgMode) ?: cardBgMode
            if (cardBgStatus)
                cardItemBg = when (cardBgMode) {
                    "light" -> ItemCardOptions.LIGHT
                    "dark" -> ItemCardOptions.DARK
                    "bg_darken" -> ItemCardOptions.DARK_WITH_BG
                    "bg_overlay" -> ItemCardOptions.LIGHT_WITH_BG
                    else -> ItemCardOptions.DARK_WITH_BG
                }
        }
    }

    @Parcelize
    enum class MovieTypeContract : Parcelable {
        DISCOVER,
        NOW_PLAYING,
        POPULAR,
        TOP_RATED,
        UPCOMING
    }

    @Parcelize
    enum class TvTypeContract : Parcelable {
        DISCOVER,
        TV_AIRING_TODAY,
        TV_OTA,
        POPULAR,
        TOP_RATED
    }

    @Parcelize
    enum class ItemCardOptions(
        val cardColor: Int,
        val imageTintColor: Int,
        val tintMode: PorterDuff.Mode,
        val itemColor: Int
    ) : Parcelable {
        LIGHT(Color.WHITE, Color.WHITE, PorterDuff.Mode.SRC_IN, DEFAULT_COLOR),
        DARK(Color.parseColor("#302E2E"), Color.parseColor("#302E2E"), PorterDuff.Mode.SRC_IN, Color.WHITE),
        DARK_WITH_BG(Color.parseColor("#302E2E"), Color.parseColor("#302E2E"), PorterDuff.Mode.DARKEN, Color.WHITE),
        LIGHT_WITH_BG(Color.parseColor("#302E2E"), Color.parseColor("#302E2E"), PorterDuff.Mode.OVERLAY, Color.WHITE)
    }

    companion object {
        const val DEFAULT_COLOR = -6565
    }

}