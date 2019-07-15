package id.apwdevs.app.catalogue.plugin.api

import id.apwdevs.app.catalogue.plugin.PublicConfig
import id.apwdevs.app.catalogue.viewModel.MainListTvViewModel

object GetTVShows {

    fun getSocmedID(idTV: Int, languageStr: String = "en-US"): String =
        "${PublicConfig.URL_API}/${PublicConfig.MOVIE_DIR_PATH}/$idTV/${PublicConfig.EXTERNAL_ID_QNAME}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}&language=$languageStr"

    fun getReviews(idTV: Int, languageStr: String = "en-US"): String =
        "${PublicConfig.URL_API}/${PublicConfig.TV_DIR_PATH}/$idTV/${PublicConfig.REVIEWS_QNAME}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}&language=$languageStr"

    fun getCredits(idTV: Int, languageStr: String = "en-US"): String =
        "${PublicConfig.URL_API}/${PublicConfig.TV_DIR_PATH}/$idTV/${PublicConfig.CREDITS_QNAME}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}&language=$languageStr"

    fun getOtherDetails(idTV: Int, languageStr: String = "en-US"): String =
        "${PublicConfig.URL_API}/${PublicConfig.TV_DIR_PATH}/$idTV?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}&language=$languageStr"

    fun getList(supportedType: MainListTvViewModel.SupportedType, pages: Int, languageStr: String = "en-US"): String =
        when (supportedType) {
            MainListTvViewModel.SupportedType.TV_AIRING_TODAY ->
                "${PublicConfig.URL_API}/${PublicConfig.TV_DIR_PATH}/${PublicConfig.TV_AIRING_TODAY}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}&language=$languageStr&page=$pages"

            MainListTvViewModel.SupportedType.TV_OTA ->
                "${PublicConfig.URL_API}/${PublicConfig.TV_DIR_PATH}/${PublicConfig.TV_OTA}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}&language=$languageStr&page=$pages"

            MainListTvViewModel.SupportedType.POPULAR ->
                "${PublicConfig.URL_API}/${PublicConfig.TV_DIR_PATH}/${PublicConfig.POPULAR}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}&language=$languageStr&page=$pages"

            MainListTvViewModel.SupportedType.DISCOVER ->
                "${PublicConfig.URL_API}/${PublicConfig.DISCOVER_PATH}/tv?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}&language=$languageStr&page=$pages"
            MainListTvViewModel.SupportedType.TOP_RATED ->
                "${PublicConfig.URL_API}/${PublicConfig.TV_DIR_PATH}/${PublicConfig.TOP_RATED}?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}&language=$languageStr&page=$pages"

        }

    fun getAllGenre(languageString: String = "en-US"): String =
        "${PublicConfig.URL_API}/${PublicConfig.GENRE}/${PublicConfig.TV_DIR_PATH}/list?${PublicConfig.API_KEY_QNAME}=${PublicConfig.API_KEY}&language=$languageString"


}