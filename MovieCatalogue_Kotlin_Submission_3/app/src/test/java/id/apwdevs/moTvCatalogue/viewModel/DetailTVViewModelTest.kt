package id.apwdevs.moTvCatalogue.viewModel

import android.app.Activity
import android.content.Intent
import com.androidnetworking.common.Priority
import id.apwdevs.moTvCatalogue.model.onUserMain.TvAboutModel
import id.apwdevs.moTvCatalogue.plugin.api.ApiRepository
import id.apwdevs.moTvCatalogue.plugin.api.GetTVShows
import id.apwdevs.moTvCatalogue.testPlugins.TestCoroutineContextProvider
import id.apwdevs.moTvCatalogue.testPlugins.getResponse
import id.apwdevs.moTvCatalogue.view.MainDetailView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.io.File

class DetailTVViewModelTest {

    private val idTv = 1412

    @Mock
    private lateinit var activity: Activity

    @Mock
    private lateinit var apiRepository: ApiRepository

    @Mock
    private lateinit var view: MainDetailView

    private lateinit var viewModel: DetailTVViewModel

    private val cacheFilePath = File("tests/json")

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        viewModel = DetailTVViewModel()
    }

    @Test
    fun testTvGetSocmedID() {
        val returnedResults = ApiRepository.ReturnedResults(
            getResponse(cacheFilePath, "resulttv_details_socmed.json"),
            null,
            true
        )
        runBlocking {
            Mockito.`when`(
                apiRepository.doRequestAndReturnJSON(
                    GetTVShows.getSocmedID(idTv),
                    "getSocmedTVId$idTv",
                    Priority.HIGH
                )
            ).thenReturn(GlobalScope.async { returnedResults })
            val results = viewModel.getSocmedID(apiRepository, idTv)

            // null if success
            assertEquals(null, results)
        }
    }

    @Test
    fun testGetTvOtherDetails() {
        val returnedResults = ApiRepository.ReturnedResults(
            getResponse(cacheFilePath, "resulttv_details.json"),
            null,
            true
        )
        runBlocking {
            Mockito.`when`(
                apiRepository.doRequestAndReturnJSON(
                    GetTVShows.getOtherDetails(idTv),
                    "getTvOtherDetailsId$idTv",
                    Priority.HIGH
                )
            ).thenReturn(GlobalScope.async { returnedResults })
            val results = viewModel.otherDetails(apiRepository, idTv)

            // null if success
            assertEquals(null, results)

        }
    }

    @Test
    fun testGetTvCredits() {
        val returnedResults = ApiRepository.ReturnedResults(
            getResponse(cacheFilePath, "resulttv_details_credits.json"),
            null,
            true
        )
        runBlocking {
            Mockito.`when`(
                apiRepository.doRequestAndReturnJSON(
                    GetTVShows.getCredits(idTv),
                    "getCreditsMoviesId$idTv",
                    Priority.HIGH
                )
            ).thenReturn(GlobalScope.async { returnedResults })
            val results = viewModel.getCredits(apiRepository, idTv)

            // null if success
            assertEquals(null, results)

        }
    }

    @Test
    fun testGetReviews() {
        val returnedResults = ApiRepository.ReturnedResults(
            getResponse(cacheFilePath, "resulttv_details_reviews.json"),
            null,
            true
        )
        runBlocking {
            Mockito.`when`(
                apiRepository.doRequestAndReturnJSON(
                    GetTVShows.getReviews(idTv),
                    "getReviewsTVId$idTv",
                    Priority.HIGH
                )
            ).thenReturn(GlobalScope.async { returnedResults })
            val results = viewModel.getReviews(apiRepository, idTv)

            // null if success
            assertEquals(null, results)
        }
    }

    @Test
    fun testSetAll() {
        val dispatchers = TestCoroutineContextProvider()
        runBlocking {

            Mockito.`when`(activity.intent).thenReturn(Intent().apply {
                putExtra(
                    "TV_DETAILS", TvAboutModel(
                        null,
                        4.0,
                        idTv,
                        null,
                        9.0,
                        null,
                        null,
                        null,
                        null,
                        null,
                        22,
                        null,
                        null

                    )
                )
            })
            Mockito.`when`(
                apiRepository.doRequestAndReturnJSON(
                    GetTVShows.getSocmedID(idTv),
                    "getSocmedTVId$idTv",
                    Priority.HIGH
                )
            ).thenReturn(GlobalScope.async {
                ApiRepository.ReturnedResults(
                    getResponse(cacheFilePath, "resulttv_details_socmed.json"),
                    null,
                    true
                )
            })

            Mockito.`when`(
                apiRepository.doRequestAndReturnJSON(
                    GetTVShows.getOtherDetails(idTv),
                    "getTvOtherDetailsId$idTv",
                    Priority.HIGH
                )
            ).thenReturn(GlobalScope.async {
                ApiRepository.ReturnedResults(
                    getResponse(cacheFilePath, "resulttv_details.json"),
                    null,
                    true
                )
            })

            Mockito.`when`(
                apiRepository.doRequestAndReturnJSON(
                    GetTVShows.getCredits(idTv),
                    "getCreditsMoviesId$idTv",
                    Priority.HIGH
                )
            ).thenReturn(GlobalScope.async {
                ApiRepository.ReturnedResults(
                    getResponse(cacheFilePath, "resulttv_details_credits.json"),
                    null,
                    true
                )
            })

            Mockito.`when`(
                apiRepository.doRequestAndReturnJSON(
                    GetTVShows.getReviews(idTv),
                    "getReviewsTVId$idTv",
                    Priority.HIGH
                )
            ).thenReturn(GlobalScope.async {
                ApiRepository.ReturnedResults(
                    getResponse(cacheFilePath, "resulttv_details_reviews.json"),
                    null,
                    true
                )
            })
            viewModel.setAll(activity, apiRepository, idTv, view, dispatchers)

            // we need to verify methods
            Mockito.verify(view).onStart()
            Mockito.verify(view).onLoad()
            Mockito.verify(view).onLoadFinished(viewModel)
        }
    }
}