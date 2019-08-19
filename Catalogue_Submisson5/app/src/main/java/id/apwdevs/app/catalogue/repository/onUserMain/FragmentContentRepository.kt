package id.apwdevs.app.catalogue.repository.onUserMain

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.androidnetworking.AndroidNetworking
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

    companion object {
        const val TAG_LOAD_RES_OBJ = "PrimaryObjectLoads"
        const val TAG_LOAD_RES_GENRE = "LoadModelGenre"

        const val MAX_FACTOR = 100
        const val GENRE_LOAD_FACTOR = 20
        const val RES_OBJ_LOAD_FACTOR = 80
    }

    val objData: MutableLiveData<T> = MutableLiveData()
    val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    var allGenre: LiveData<List<GenreModel>>? = null
    var retError: MutableLiveData<GetObjectFromServer.RetError> = MutableLiveData()
    val inSearchMode: MutableLiveData<Boolean> = MutableLiveData()
    //val searchAPIResult: MutableLiveData<T> = MutableLiveData()
    val loadProgress: MutableLiveData<Double> = MutableLiveData()
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
            loadProgress.postValue(0.0)
            isLoading.postValue(true)
            val db = FavoriteDatabase.getInstance(context)
            val objServer = GetObjectFromServer.getInstance(context)
            val getGenre = getGenreAsync(db, objServer)
            val obj = when (type) {
                PublicContract.ContentDisplayType.MOVIE -> {
                    loadFromInet(
                        GetMovies.getList(commands[0] as MainListViewModel.MovieTypeContract),
                        "GetListMovie${commands[0]}",
                        objServer
                    )
                }
                PublicContract.ContentDisplayType.TV_SHOWS -> {
                    loadFromInet(
                        GetTVShows.getList(commands[0] as MainListViewModel.TvTypeContract, 1),
                        "GetListTv${commands[0]}",
                        objServer
                    )
                }
                PublicContract.ContentDisplayType.FAVORITES -> {
                    when (commands[0]) {
                        PublicContract.ContentDisplayType.TV_SHOWS -> {
                            FavoriteResponse(
                                listAll = db.favoriteDao().getAsType(PublicContract.ContentDisplayType.TV_SHOWS.type)
                            ) as T
                        }
                        PublicContract.ContentDisplayType.MOVIE -> {
                            FavoriteResponse(
                                listAll = db.favoriteDao().getAsType(PublicContract.ContentDisplayType.MOVIE.type)
                            ) as T
                        }
                        else -> null
                    }
                }
            }
            while (!getGenre.isCompleted) delay(200)
            applyGenreIntoModels(obj)
            checkAndApplyFavorite(obj, db)
            objData.postValue(obj)
            isLoading.postValue(false)
        }
    }

    // from 1 until 80
    private fun loadFromInet(url: String, tag: String, objServer: GetObjectFromServer): T? {
        var progress = 0.0
        val callbacks = object : GetObjectFromServer.GetObjectFromServerCallback<T> {
            override fun onSuccess(response: T) {
            }

            override fun onFailed(retError: GetObjectFromServer.RetError) {
                this@FragmentContentRepository.onFailed(retError)
            }

            override fun onProgress(percent: Double) {
                val currProgress = percent * RES_OBJ_LOAD_FACTOR / MAX_FACTOR
                this@FragmentContentRepository.onProgress(currProgress - progress)
                progress = currProgress
            }

        }

        return objServer.getSynchronousObj(
            url,
            MainDataItemResponse::class.java as Class<T>,
            tag,
            callbacks
        )?.apply {
            if (this is MainDataItemResponse)
                contents?.forEach {
                    it.contentTypes = type.type
                }
        }
    }
    @WorkerThread
    private suspend fun checkAndApplyFavorite(value: T?, db: FavoriteDatabase) =
        withContext(Dispatchers.Default) {
            value?.let {
                val favDao = db.favoriteDao()
                when (it) {
                    is MainDataItemResponse ->
                        it.contents?.forEach { dModel ->
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
                it.contents?.forEach { content ->
                    content.actualGenreModel = mutableListOf()
                    content.genres.forEach { contentGenre ->
                        for (modelGenre in requireNotNull(allGenre?.value)) {
                            if (modelGenre.id == contentGenre) {
                                content.actualGenreModel?.add(modelGenre)
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
        //if(allGenre?.value != null)return@async
        val gDao = db.genreDao()
        if (gDao.size() == 0 || forceUpdate) {
            updateGenreFromInetAsync(gDao, objectFromServer).await()
        }
        allGenre = MutableLiveData(gDao.getAll())
    }

    // FROM 1..20 PROGRESS
    private fun updateGenreFromInetAsync(
        gDao: GenreDao,
        objectFromServer: GetObjectFromServer,
        priority: Priority = Priority.MEDIUM
    ) = GlobalScope.async {

        var progress = 0.0
        val callbacks = object : GetObjectFromServer.GetObjectFromServerCallback<GenreModelResponse> {
            // already returned from objects
            override fun onSuccess(response: GenreModelResponse) {
            }

            override fun onFailed(retError: GetObjectFromServer.RetError) {
                retError.cause?.printStackTrace()
            }

            override fun onProgress(percent: Double) {
                val currProgress = percent * (GENRE_LOAD_FACTOR / 2) / MAX_FACTOR
                this@FragmentContentRepository.onProgress(currProgress - progress)
                progress = currProgress

            }

        }
        val genreMovieObj = objectFromServer.getSynchronousObj(
            GetMovies.getAllGenre(),
            GenreModelResponse::class.java,
            "GetAllMovieGenre",
            callbacks,
            priority
        )
        val genreTvObj = objectFromServer.getSynchronousObj(
            GetTVShows.getAllGenre(),
            GenreModelResponse::class.java,
            "GetAllTvGenre",
            callbacks,
            priority
        )
        genreMovieObj?.let {
            addGenreIntoDb1Async(gDao, it).await()
        }
        genreTvObj?.let {
            addGenreIntoDb1Async(gDao, it).await()
        }
    }

    @WorkerThread
    private fun addGenreIntoDb1Async(gDao: GenreDao, response: GenreModelResponse) = GlobalScope.async {
        gDao.addAll(response.allGenre)
    }

    override fun onSuccess(response: T) {
        /*if (response is MainDataItemResponse)
            response.contents?.forEach {
                it.contentTypes = type.type
            }
        objData.postValue(response)*/
    }

    override fun onFailed(retError: GetObjectFromServer.RetError) {
        isLoading.postValue(false)
        this.retError.postValue(retError)
    }

    override fun onProgress(percent: Double) {
        // increment progress
        Log.d("PROGRESS", "CUrrent Progress -> ${(loadProgress.value ?: 0.0) + percent}")
        loadProgress.postValue(
            (loadProgress.value ?: 0.0) + percent
        )
    }

    fun forceLoadIn(content: T?) {
        viewModelScope.launch(Dispatchers.IO) {
            retError.postValue(null)
            loadProgress.postValue(0.0)
            isLoading.postValue(true)
            val db = FavoriteDatabase.getInstance(context)
            val objServer = GetObjectFromServer.getInstance(context)
            getGenreAsync(db, objServer).await()
            loadProgress.postValue(RES_OBJ_LOAD_FACTOR.toDouble())
            objData.postValue(content)
            applyGenreIntoModels(objData.value)
            checkAndApplyFavorite(objData.value, db)
            isLoading.postValue(false)
        }
    }

    fun requestSearch(search: String) {
        viewModelScope.launch(Dispatchers.IO) {
            inSearchMode.postValue(true)
            loadProgress.postValue(0.0)
            isLoading.postValue(true)
            val db = FavoriteDatabase.getInstance(context)
            val objServer = GetObjectFromServer.getInstance(context)
            var progress = 0.0
            val obj = objServer
                .getSynchronousObj(
                    search,
                    MainDataItemResponse::class.java,
                    "SearchSynchronous",
                    object : GetObjectFromServer.GetObjectFromServerCallback<MainDataItemResponse> {
                        override fun onSuccess(response: MainDataItemResponse) {

                        }

                        override fun onFailed(retError: GetObjectFromServer.RetError) {
                            this@FragmentContentRepository.retError.postValue(retError)
                        }

                        override fun onProgress(percent: Double) {
                            val currProgress = percent * RES_OBJ_LOAD_FACTOR / MAX_FACTOR
                            this@FragmentContentRepository.onProgress(currProgress - progress)
                            progress = currProgress
                        }

                    }) as T?
            getGenreAsync(db, objServer).await()
            if (inSearchMode.value != false) {
                applyGenreIntoModels(obj)
                checkAndApplyFavorite(obj, db)
                objData.postValue(obj)
                isLoading.postValue(false)
                //inSearchMode.postValue(false)
            }
        }
    }

    fun forceEndSearch() {
        inSearchMode.value = false
        isLoading.value = false
        AndroidNetworking.cancel("SearchSynchronous")
    }


}