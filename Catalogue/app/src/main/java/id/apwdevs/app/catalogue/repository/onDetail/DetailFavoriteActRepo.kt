package id.apwdevs.app.catalogue.repository.onDetail

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import id.apwdevs.app.catalogue.activities.DetailActivity
import id.apwdevs.app.catalogue.database.FavoriteDatabase
import id.apwdevs.app.catalogue.entity.FavoriteEntity
import id.apwdevs.app.catalogue.model.GenreModel
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onDetail.OtherMovieAboutModel
import id.apwdevs.app.catalogue.model.onDetail.OtherTVAboutModel
import id.apwdevs.app.catalogue.model.onUserMain.MovieAboutModel
import id.apwdevs.app.catalogue.model.onUserMain.TvAboutModel
import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.plugin.api.GetMovies
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer
import id.apwdevs.app.catalogue.plugin.api.GetTVShows
import kotlinx.coroutines.*

class DetailFavoriteActRepo(
    mContext: Context,
    viewModelScope: CoroutineScope
) : DetailActivityRepository(mContext, viewModelScope) {
    private var typeContent: MutableLiveData<TypeContentContract> = MutableLiveData()
    override val typeContentContract: TypeContentContract
        get() = typeContent.value ?: TypeContentContract.MOVIE
    private var favEntity: FavoriteEntity? = null

    override fun initAtFirstTime(dataIntent: Intent) {
        favEntity = dataIntent.extras?.getParcelable<FavoriteEntity>(DetailActivity.EXTRA_CONTENT_DETAILS)?.also {
            typeContent.value = requireNotNull(
                when (PublicContract.ContentDisplayType.findId(it.contentType)) {
                    PublicContract.ContentDisplayType.MOVIE -> TypeContentContract.MOVIE
                    PublicContract.ContentDisplayType.TV_SHOWS -> TypeContentContract.TV_SHOWS
                    else -> null
                }
            )
        }
    }

    override fun getDataAsync(id: Int): Deferred<Boolean> = GlobalScope.async {
        val getObjectRepo = GetObjectFromServer.getInstance(context)
        val genreDao = FavoriteDatabase.getInstance(context).genreDao()
        favEntity?.apply {
            var isFinished = false
            getObjectRepo.getResponseAsString(
                when (typeContent.value) {
                    TypeContentContract.MOVIE -> GetMovies.getOtherDetails(id)
                    TypeContentContract.TV_SHOWS -> GetTVShows.getOtherDetails(id)
                    else -> ""
                },
                "GetFavoriteDetails",
                object : GetObjectFromServer.GetObjectFromServerCallback<String?> {
                    override fun onSuccess(response: String?) {
                        // we have to put into worker threads
                        response?.let {
                            viewModelScope.launch(Dispatchers.IO) {
                                val typeBuff = Gson().fromJson(it, TempBuff::class.java)
                                val otherModel = when (typeContent.value) {
                                    TypeContentContract.MOVIE -> Gson().fromJson(it, OtherMovieAboutModel::class.java)
                                    TypeContentContract.TV_SHOWS -> Gson().fromJson(it, OtherTVAboutModel::class.java)
                                    else -> null
                                } as ResettableItem

                                val (oriTitle, oriLang, backdropPath) = typeBuff

                                val genreModels = mutableListOf<GenreModel>()
                                if (genreIds?.isNotEmpty() == true) {
                                    genreIds.split(",").forEach {
                                        genreModels.add(
                                            GenreModel(
                                                genreDao.getGenreIdByName(it),
                                                it
                                            )
                                        )
                                    }
                                }

                                // build a models
                                data1Obj.postValue(
                                    when (typeContent.value) {
                                        TypeContentContract.MOVIE -> MovieAboutModel(
                                            id = id,
                                            releaseDate = releaseDate,
                                            posterPath = posterPath,
                                            overview = overview,
                                            genres = listOf(), // empty list
                                            originalTitle = oriTitle,
                                            originalLanguage = oriLang,
                                            title = title,
                                            backdropPath = backdropPath,
                                            voteCount = voteCount,
                                            voteAverage = voteAverage,
                                            isFavorite = true,
                                            actualGenreModel = genreModels
                                        )
                                        TypeContentContract.TV_SHOWS -> TvAboutModel(
                                            idTv = id,
                                            firstAirDate = releaseDate,
                                            posterPath = posterPath,
                                            overview = overview,
                                            genres = listOf(), // empty list because it couldn't be used, move into actualGenreModel instead
                                            originalName = oriTitle,
                                            originalLanguage = oriLang,
                                            name = title,
                                            backdropPath = backdropPath,
                                            voteCount = voteCount,
                                            voteAverage = voteAverage,
                                            isFavorite = true,
                                            actualGenreModel = genreModels
                                        )
                                        else -> null
                                    }
                                )

                                data2Obj.postValue(otherModel)
                            }
                        }
                        isFinished = true
                    }

                    override fun onFailed(retError: GetObjectFromServer.RetError) {
                        hasLoading.postValue(false)
                        this@DetailFavoriteActRepo.retError.postValue(retError)
                    }

                    override fun onProgress(percent: Double) {
                    }

                }
            )
            while (!isFinished) delay(300)
            return@async true
        }
        false
    }

    internal data class TempBuff(

        @SerializedName("original_title", alternate = ["original_name"])
        val originalTitle: String?,

        @SerializedName("original_language")
        val originalLanguage: String?,

        @SerializedName("backdrop_path")
        val backdropPath: String?
    )
}