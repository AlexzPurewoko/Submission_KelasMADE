package id.apwdevs.moTvCatalogue.viewModel

import android.app.Activity
import android.content.Intent
import com.androidnetworking.common.Priority
import id.apwdevs.moTvCatalogue.model.onUserMain.MovieAboutModel
import id.apwdevs.moTvCatalogue.plugin.api.ApiRepository
import id.apwdevs.moTvCatalogue.plugin.api.GetMovies
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
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.io.File

class DetailMovieViewModelTestUnit {

    val idMovies = 301528

    @Mock
    private lateinit var activity: Activity

    @Mock
    private lateinit var apiRepository: ApiRepository

    @Mock
    private lateinit var view: MainDetailView

    private lateinit var viewModel: DetailMovieViewModel

    private val cacheFilePath = File("tests/json")
    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        viewModel = spy(DetailMovieViewModel())
    }

    @Test
    fun getMovieSocmedID() {
        val returnedResults = ApiRepository.ReturnedResults(
            getResponse(cacheFilePath, "get_details_socmed.json"),
            null,
            true
        )
        runBlocking {
            `when`(
                apiRepository.doRequestAndReturnJSON(
                    GetMovies.getSocmedID(idMovies),
                    "getSocmedMoviesId$idMovies",
                    Priority.HIGH
                )
            ).thenReturn(GlobalScope.async { returnedResults })
            val results = viewModel.setListSocmedId(apiRepository, idMovies)

            // null if success
            assertEquals(null, results)
        }
    }

    @Test
    fun getMovieOtherDetails() {
        val returnedResults = ApiRepository.ReturnedResults(
            getResponse(cacheFilePath, "get_details.json"),
            null,
            true
        )
        runBlocking {
            `when`(
                apiRepository.doRequestAndReturnJSON(
                    GetMovies.getOtherDetails(idMovies),
                    "getOtherDetails$idMovies",
                    Priority.HIGH
                )
            ).thenReturn(GlobalScope.async { returnedResults })
            val results = viewModel.otherDetails(apiRepository, idMovies)

            // null if success
            assertEquals(null, results)

        }
    }

    @Test
    fun testSetCredits() {
        val returnedResults = ApiRepository.ReturnedResults(
            getResponse(cacheFilePath, "get_details_credit.json"),
            null,
            true
        )
        runBlocking {
            `when`(
                apiRepository.doRequestAndReturnJSON(
                    GetMovies.getCredits(idMovies),
                    "getCreditsMoviesId$idMovies",
                    Priority.HIGH
                )
            ).thenReturn(GlobalScope.async { returnedResults })
            val results = viewModel.setCredits(apiRepository, idMovies)

            // null if success
            assertEquals(null, results)

        }
    }

    @Test
    fun getMovieReviews() {
        val returnedResults = ApiRepository.ReturnedResults(
            getResponse(cacheFilePath, "get_details_reviews.json"),
            null,
            true
        )
        runBlocking {
            `when`(
                apiRepository.doRequestAndReturnJSON(
                    GetMovies.getReviews(idMovies),
                    "getReviewsMoviesId$idMovies",
                    Priority.HIGH
                )
            ).thenReturn(GlobalScope.async { returnedResults })
            val results = viewModel.setReviews(apiRepository, idMovies)

            // null if success
            assertEquals(null, results)
        }
    }

    @Test
    fun testSetAll() {
        val dispatchers = TestCoroutineContextProvider()
        runBlocking {

            `when`(activity.intent).thenReturn(Intent().apply {
                putExtra(
                    "MOVIE_DETAILS", MovieAboutModel(
                        idMovies,
                        null,
                        null,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        22.0,
                        10,
                        8.5,
                        false

                    )
                )
            })
            `when`(
                apiRepository.doRequestAndReturnJSON(
                    GetMovies.getSocmedID(idMovies),
                    "getSocmedMoviesId$idMovies",
                    Priority.HIGH
                )
            ).thenReturn(GlobalScope.async {
                ApiRepository.ReturnedResults(
                    getResponse(cacheFilePath, "get_details_socmed.json"),
                    null,
                    true
                )
            })

            `when`(
                apiRepository.doRequestAndReturnJSON(
                    GetMovies.getOtherDetails(idMovies),
                    "getOtherDetails$idMovies",
                    Priority.HIGH
                )
            ).thenReturn(GlobalScope.async {
                ApiRepository.ReturnedResults(
                    getResponse(cacheFilePath, "get_details.json"),
                    null,
                    true
                )
            })

            `when`(
                apiRepository.doRequestAndReturnJSON(
                    GetMovies.getCredits(idMovies),
                    "getCreditsMoviesId$idMovies",
                    Priority.HIGH
                )
            ).thenReturn(GlobalScope.async {
                ApiRepository.ReturnedResults(
                    getResponse(cacheFilePath, "get_details_credit.json"),
                    null,
                    true
                )
            })

            `when`(
                apiRepository.doRequestAndReturnJSON(
                    GetMovies.getReviews(idMovies),
                    "getReviewsMoviesId$idMovies",
                    Priority.HIGH
                )
            ).thenReturn(GlobalScope.async {
                ApiRepository.ReturnedResults(
                    getResponse(cacheFilePath, "get_details_reviews.json"),
                    null,
                    true
                )
            })
            viewModel.setAll(activity, apiRepository, idMovies, view, dispatchers)

            // we need to verify methods
            verify(view).onStart()
            verify(view).onLoad()
            verify(view).onLoadFinished(viewModel)
        }
    }

}