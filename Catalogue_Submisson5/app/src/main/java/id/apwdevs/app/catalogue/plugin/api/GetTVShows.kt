package id.apwdevs.app.catalogue.plugin.api

import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.viewModel.MainListViewModel.TvTypeContract
import java.util.*

object GetTVShows {

    fun getSocmedID(idTV: Int, languageStr: String = "en-US"): String =
        "${PublicContract.URL_API}/${PublicContract.MOVIE_DIR_PATH}/$idTV/${PublicContract.EXTERNAL_ID_QNAME}?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}&language=$languageStr"

    fun getReviews(idTV: Int, languageStr: String = "en-US"): String =
        "${PublicContract.URL_API}/${PublicContract.TV_DIR_PATH}/$idTV/${PublicContract.REVIEWS_QNAME}?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}&language=$languageStr"

    fun getCredits(idTV: Int, languageStr: String = "en-US"): String =
        "${PublicContract.URL_API}/${PublicContract.TV_DIR_PATH}/$idTV/${PublicContract.CREDITS_QNAME}?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}&language=$languageStr"

    fun getOtherDetails(idTV: Int, languageStr: String = "en-US"): String =
        "${PublicContract.URL_API}/${PublicContract.TV_DIR_PATH}/$idTV?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}&language=$languageStr"

    fun getList(supportedType: TvTypeContract, pages: Int, languageStr: String = "en-US"): String =
        when (supportedType) {
            TvTypeContract.TV_AIRING_TODAY ->
                "${PublicContract.URL_API}/${PublicContract.TV_DIR_PATH}/${PublicContract.TV_AIRING_TODAY}?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}&language=$languageStr&page=$pages"

            TvTypeContract.TV_OTA ->
                "${PublicContract.URL_API}/${PublicContract.TV_DIR_PATH}/${PublicContract.TV_OTA}?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}&language=$languageStr&page=$pages"

            TvTypeContract.POPULAR ->
                "${PublicContract.URL_API}/${PublicContract.TV_DIR_PATH}/${PublicContract.POPULAR}?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}&language=$languageStr&page=$pages"

            TvTypeContract.DISCOVER ->
                "${PublicContract.URL_API}/${PublicContract.DISCOVER_PATH}/tv?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}&language=$languageStr&page=$pages"
            TvTypeContract.TOP_RATED ->
                "${PublicContract.URL_API}/${PublicContract.TV_DIR_PATH}/${PublicContract.TOP_RATED}?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}&language=$languageStr&page=$pages"

        }

    fun getAllGenre(languageString: String = "en-US"): String =
        "${PublicContract.URL_API}/${PublicContract.GENRE}/${PublicContract.TV_DIR_PATH}/list?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}&language=$languageString"


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
        return "${PublicContract.URL_API}/${PublicContract.DISCOVER_PATH}/${PublicContract.TV_DIR_PATH}?${PublicContract.API_KEY_QNAME}=${PublicContract.API_KEY}&first_air_date.gte=$mDate&first_air_date.lte=$mDate"
    }
}