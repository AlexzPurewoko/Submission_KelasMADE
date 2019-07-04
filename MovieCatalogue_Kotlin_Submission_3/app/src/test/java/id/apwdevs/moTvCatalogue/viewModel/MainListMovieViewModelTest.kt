package id.apwdevs.moTvCatalogue.viewModel

import com.androidnetworking.common.Priority
import id.apwdevs.moTvCatalogue.plugin.api.ApiRepository
import id.apwdevs.moTvCatalogue.plugin.api.GetMovies
import id.apwdevs.moTvCatalogue.testPlugins.TestCoroutineContextProvider
import id.apwdevs.moTvCatalogue.testPlugins.getResponse
import id.apwdevs.moTvCatalogue.view.MainUserListView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.io.File

class MainListMovieViewModelTest {

    @Mock
    private lateinit var apiRepository: ApiRepository

    @Mock
    private lateinit var view: MainUserListView

    private lateinit var viewModel: MainListMovieViewModel
    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        viewModel = MainListMovieViewModel()
    }

    @Test
    fun testSetup() {
        runBlocking {
            val pages = 1
            val tag = "DiscoverMovie"
            val supportedType = MainListMovieViewModel.SupportedType.DISCOVER
            val path = File("tests/json")
            val returnedResults = ApiRepository.ReturnedResults(
                getResponse(path, "discover_movies_desc_popularity_sort.json"),
                null,
                true
            )

            Mockito.`when`(
                apiRepository.doRequestAndReturnJSON(
                    GetMovies.getAllGenre(),
                    "GetAllMovieGenre",
                    Priority.MEDIUM
                )
            ).thenReturn(GlobalScope.async {
                ApiRepository.ReturnedResults(
                    getResponse(path, "all_genres.json"),
                    null,
                    true
                )
            })
            Mockito.`when`(
                apiRepository.doRequestAndReturnJSON(
                    GetMovies.getList(
                        supportedType,
                        pages
                    ),
                    "${tag}Pages$pages",
                    Priority.LOW
                )
            ).thenReturn(GlobalScope.async { returnedResults })

            viewModel.setup(
                apiRepository,
                MainListMovieViewModel.SupportedType.DISCOVER,
                pages,
                tag,
                view,
                TestCoroutineContextProvider()
            )

            Mockito.verify(view).onStart()
            Mockito.verify(view).onLoadFinished()
            Mockito.verify(view).onLoadSuccess(viewModel)
        }
    }
}