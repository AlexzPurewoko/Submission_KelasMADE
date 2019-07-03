package id.apwdevs.moTvCatalogue.plugin.api

import id.apwdevs.moTvCatalogue.plugin.PublicConfig

object GetMovies {
    fun getSocmedID(idMovies: Int): String =
        "${PublicConfig.URL_API}/${PublicConfig.MOVIE_DIR_PATH}/$idMovies/${PublicConfig.EXTERNAL_ID_QNAME}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}"

    fun getReviews(idMovies: Int): String =
        "${PublicConfig.URL_API}/${PublicConfig.MOVIE_DIR_PATH}/$idMovies/${PublicConfig.REVIEWS_QNAME}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}"

    fun getCredits(idMovies: Int): String =
        "${PublicConfig.URL_API}/${PublicConfig.MOVIE_DIR_PATH}/$idMovies/${PublicConfig.CREDITS_QNAME}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}"

    fun getOtherDetails(idMovies: Int): String =
        "${PublicConfig.URL_API}/${PublicConfig.MOVIE_DIR_PATH}/$idMovies?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}"

}