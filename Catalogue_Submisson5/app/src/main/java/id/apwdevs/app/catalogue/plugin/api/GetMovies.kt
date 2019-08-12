package id.apwdevs.app.catalogue.plugin.api

import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.viewModel.MainListViewModel.MovieTypeContract
import java.util.*

object GetMovies {
    fun getSocmedID(idMovies: Int): String =
        "${PublicContract.URL_API}/${PublicContract.MOVIE_DIR_PATH}/$idMovies/${PublicContract.EXTERNAL_ID_QNAME}?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}"

    fun getReviews(idMovies: Int): String =
        "${PublicContract.URL_API}/${PublicContract.MOVIE_DIR_PATH}/$idMovies/${PublicContract.REVIEWS_QNAME}?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}"

    fun getCredits(idMovies: Int): String =
        "${PublicContract.URL_API}/${PublicContract.MOVIE_DIR_PATH}/$idMovies/${PublicContract.CREDITS_QNAME}?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}"

    fun getOtherDetails(idMovies: Int): String =
        "${PublicContract.URL_API}/${PublicContract.MOVIE_DIR_PATH}/$idMovies?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}"

    fun getList(types: MovieTypeContract, pages: Int = 1, languageString: String = "en-US"): String =
        when (types) {
            MovieTypeContract.DISCOVER ->
                "${PublicContract.URL_API}/${PublicContract.DISCOVER_PATH}/${PublicContract.MOVIE_DIR_PATH}?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}&language=$languageString&page=$pages"
            MovieTypeContract.NOW_PLAYING ->
                "${PublicContract.URL_API}/${PublicContract.MOVIE_DIR_PATH}/${PublicContract.NOW_PLAYING}?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}&language=$languageString&page=$pages"
            MovieTypeContract.POPULAR ->
                "${PublicContract.URL_API}/${PublicContract.MOVIE_DIR_PATH}/${PublicContract.POPULAR}?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}&language=$languageString&page=$pages"
            MovieTypeContract.TOP_RATED ->
                "${PublicContract.URL_API}/${PublicContract.MOVIE_DIR_PATH}/${PublicContract.TOP_RATED}?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}&language=$languageString&page=$pages"
            MovieTypeContract.UPCOMING ->
                "${PublicContract.URL_API}/${PublicContract.MOVIE_DIR_PATH}/${PublicContract.UPCOMING}?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}&language=$languageString&page=$pages"
        }

    fun getAllGenre(languageString: String = "en-US"): String =
        "${PublicContract.URL_API}/${PublicContract.GENRE}/${PublicContract.MOVIE_DIR_PATH}/list?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}&language=$languageString"

    fun getCurrentRelease(now: Calendar): String {
        val year = now.get(Calendar.YEAR)
        val month: String
        now.get(Calendar.MONTH).apply {
            when {
                this < 10 -> month = "01"
                else -> month = this.toString()
            }
        }
        val date = now.get(Calendar.DAY_OF_MONTH)
        val mDate = "$year-$month-$date"
        return "${PublicContract.URL_API}/${PublicContract.DISCOVER_PATH}/${PublicContract.MOVIE_DIR_PATH}?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}&primary_release_date.gte=$mDate&primary_release_date.lte=$mDate"
    }

}