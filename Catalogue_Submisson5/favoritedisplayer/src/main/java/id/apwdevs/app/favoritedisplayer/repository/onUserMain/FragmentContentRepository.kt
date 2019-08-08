package id.apwdevs.app.catalogue.repository.onUserMain

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.androidnetworking.common.Priority
import id.apwdevs.app.catalogue.dao.GenreDao
import id.apwdevs.app.catalogue.database.FavoriteDatabase
import id.apwdevs.app.catalogue.entity.FavoriteResponse
import id.apwdevs.app.catalogue.model.ClassResponse
import id.apwdevs.app.catalogue.model.GenreModel
import id.apwdevs.app.catalogue.model.GenreModelResponse
import id.apwdevs.app.catalogue.model.onUserMain.MainDataItemResponse
import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.plugin.api.GetMovies
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer
import id.apwdevs.app.catalogue.plugin.api.GetTVShows
import id.apwdevs.app.catalogue.viewModel.MainListViewModel
import kotlinx.coroutines.*

@Suppress("UNCHECKED_CAST")
class FragmentContentRepository<T : ClassResponse>(
    private val context: Context,
    private val type: PublicContract.ContentDisplayType,
    private val viewModelScope: CoroutineScope
) : GetObjectFromServer.GetObjectFromServerCallback<T> {

    val objData: MutableLiveData<T> = MutableLiveData()
    val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    var allGenre: LiveData<List<GenreModel>>? = null
    var retError: MutableLiveData<GetObjectFromServer.RetError> = MutableLiveData()

    init {
        isLoading.value = false
    }

    /**
     * Load arguments
     * @param commands [0] is type of Contract
     * @param commands [1] is Integer of pages
     *
     */
    fun load(vararg commands: Any) {
        viewModelScope.launch(Dispatchers.IO) {
            retError.postValue(null)
            isLoading.postValue(true)
            val db = FavoriteDatabase.getInstance(context)
            val objServer = GetObjectFromServer.getInstance(context)
            val getGenre = getGenreAsync(db, objServer)
            when (type) {
                PublicContract.ContentDisplayType.MOVIE -> {
                    objServer.getObj(
                        GetMovies.getList(commands[0] as MainListViewModel.MovieTypeContract),
                        MainDataItemResponse::class.java as Class<T>,
                        "GetListMovie${commands[0]}",
                        this@FragmentContentRepository
                    )
                }
                PublicContract.ContentDisplayType.TV_SHOWS -> {
                    objServer.getObj(
                        GetTVShows.getList(commands[0] as MainListViewModel.TvTypeContract, 1),
                        MainDataItemResponse::class.java as Class<T>,
                        "GetListTv${commands[0]}",
                        this@FragmentContentRepository
                    )

                }
                PublicContract.ContentDisplayType.FAVORITES -> {
                    when (commands[0]) {
                        PublicContract.ContentDisplayType.TV_SHOWS -> {
                            objData.postValue(
                                FavoriteResponse(
                                    listAll = db.favoriteDao().getAsType(PublicContract.ContentDisplayType.TV_SHOWS.type)
                                ) as T
                            )
                        }
                        PublicContract.ContentDisplayType.MOVIE -> {
                            objData.postValue(
                                FavoriteResponse(
                                    listAll = db.favoriteDao().getAsType(PublicContract.ContentDisplayType.MOVIE.type)
                                ) as T
                            )
                        }
                    }
                }
            }
            while (getGenre.isActive) delay(300)
            applyGenreIntoModels(objData.value)
            checkAndApplyFavorite(objData.value, db)
            isLoading.postValue(false)
        }
    }

    @WorkerThread
    private suspend fun checkAndApplyFavorite(value: T?, db: FavoriteDatabase) =
        withContext(Dispatchers.Default) {
            value?.let {
                val favDao = db.favoriteDao()
                when (it) {
                    is MainDataItemResponse ->
                        it.contents.forEach { dModel ->
                            dModel.isFavorite = favDao.isAnyColumnIn(dModel.id)
                        }
                    else -> {
                    }
                }
            }
        }


    private fun applyGenreIntoModels(response: T?) {
        response?.let {
            if (it is MainDataItemResponse)
                it.contents.forEach { content ->
                    content.actualGenreModel = mutableListOf()
                    content.genres.forEach { contentGenre ->
                        for (modelGenre in requireNotNull(allGenre?.value)) {
                            if (modelGenre.id == contentGenre) {
                                content.actualGenreModel.add(modelGenre)
                                break
                            }
                        }

                    }
                }
        }


    }

    private fun getGenreAsync(
        db: FavoriteDatabase,
        objectFromServer: GetObjectFromServer,
        forceUpdate: Boolean = false
    ) = GlobalScope.async {
        val gDao = db.genreDao()
        if (gDao.size() == 0 || forceUpdate) {
            updateGenreFromInetAsync(gDao, objectFromServer).await()
        }
        allGenre = MutableLiveData(gDao.getAll())
    }

    private fun updateGenreFromInetAsync(
        gDao: GenreDao,
        objectFromServer: GetObjectFromServer,
        priority: Priority = Priority.MEDIUM
    ) = GlobalScope.async {

        var isFinished = false
        objectFromServer.getObj(
            GetMovies.getAllGenre(),
            GenreModelResponse::class.java,
            "GetAllMovieGenre",
            object : GetObjectFromServer.GetObjectFromServerCallback<GenreModelResponse> {
                override fun onSuccess(response: GenreModelResponse) {
                    viewModelScope.launch(Dispatchers.IO) {
                        addGenreIntoDb1Async(gDao, response).await()
                        isFinished = true
                    }
                }

                override fun onFailed(retError: GetObjectFromServer.RetError) {
                    retError.cause?.printStackTrace()
                }

                override fun onProgress(percent: Double) {

                }

            },
            priority
        )
        while (!isFinished) delay(400)
        isFinished = false
        objectFromServer.getObj(
            GetTVShows.getAllGenre(),
            GenreModelResponse::class.java,
            "GetAllTvGenre",
            object : GetObjectFromServer.GetObjectFromServerCallback<GenreModelResponse> {
                override fun onSuccess(response: GenreModelResponse) {
                    viewModelScope.launch(Dispatchers.IO) {
                        addGenreIntoDb1Async(gDao, response).await()
                        isFinished = true
                    }
                }

                override fun onFailed(retError: GetObjectFromServer.RetError) {
                    retError.cause?.printStackTrace()
                }

                override fun onProgress(percent: Double) {
                }

            }
        )
        while (!isFinished) delay(400)
    }

    @WorkerThread
    private fun addGenreIntoDb1Async(gDao: GenreDao, response: GenreModelResponse) = GlobalScope.async {
        gDao.addAll(response.allGenre)
    }

    override fun onSuccess(response: T) {
        objData.postValue(response)
    }

    override fun onFailed(retError: GetObjectFromServer.RetError) {
        isLoading.postValue(false)
        this.retError.value = retError
    }

    override fun onProgress(percent: Double) {
    }


}