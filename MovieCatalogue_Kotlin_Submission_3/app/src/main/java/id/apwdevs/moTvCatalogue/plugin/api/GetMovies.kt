package id.apwdevs.moTvCatalogue.plugin.api

import id.apwdevs.moTvCatalogue.plugin.PublicConfig
import id.apwdevs.moTvCatalogue.viewModel.MainListMovieViewModel

object GetMovies {
    fun getSocmedID(idMovies: Int): String =
        "${PublicConfig.URL_API}/${PublicConfig.MOVIE_DIR_PATH}/$idMovies/${PublicConfig.EXTERNAL_ID_QNAME}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}"

    fun getReviews(idMovies: Int): String =
        "${PublicConfig.URL_API}/${PublicConfig.MOVIE_DIR_PATH}/$idMovies/${PublicConfig.REVIEWS_QNAME}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}"

    fun getCredits(idMovies: Int): String =
        "${PublicConfig.URL_API}/${PublicConfig.MOVIE_DIR_PATH}/$idMovies/${PublicConfig.CREDITS_QNAME}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}"

    fun getOtherDetails(idMovies: Int): String =
        "${PublicConfig.URL_API}/${PublicConfig.MOVIE_DIR_PATH}/$idMovies?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}"

    fun getList(types: MainListMovieViewModel.SupportedType, pages: Int = 1, languageString: String = "en-US"): String =
        when (types) {
            MainListMovieViewModel.SupportedType.DISCOVER ->
                "${PublicConfig.URL_API}/${PublicConfig.DISCOVER_PATH}/${PublicConfig.MOVIE_DIR_PATH}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}&language=$languageString&page=$pages"
            MainListMovieViewModel.SupportedType.NOW_PLAYING ->
                "${PublicConfig.URL_API}/${PublicConfig.MOVIE_DIR_PATH}/${PublicConfig.NOW_PLAYING}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}&language=$languageString&page=$pages"
            MainListMovieViewModel.SupportedType.POPULAR ->
                "${PublicConfig.URL_API}/${PublicConfig.MOVIE_DIR_PATH}/${PublicConfig.POPULAR}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}&language=$languageString&page=$pages"
            MainListMovieViewModel.SupportedType.TOP_RATED ->
                "${PublicConfig.URL_API}/${PublicConfig.MOVIE_DIR_PATH}/${PublicConfig.TOP_RATED}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}&language=$languageString&page=$pages"
            MainListMovieViewModel.SupportedType.UPCOMING ->
                "${PublicConfig.URL_API}/${PublicConfig.MOVIE_DIR_PATH}/${PublicConfig.UPCOMING}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}&language=$languageString&page=$pages"
        }

    fun getAllGenre(languageString: String = "en-US"): String =
        "${PublicConfig.URL_API}/${PublicConfig.GENRE}/${PublicConfig.MOVIE_DIR_PATH}/list?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}&language=$languageString"

}