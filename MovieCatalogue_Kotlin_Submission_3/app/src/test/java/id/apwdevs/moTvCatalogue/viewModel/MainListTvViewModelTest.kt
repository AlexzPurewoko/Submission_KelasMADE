package id.apwdevs.moTvCatalogue.viewModel

import com.androidnetworking.common.Priority
import id.apwdevs.moTvCatalogue.plugin.api.ApiRepository
import id.apwdevs.moTvCatalogue.plugin.api.GetMovies
import id.apwdevs.moTvCatalogue.plugin.api.GetTVShows
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

class MainListTvViewModelTest {

    @Mock
    private lateinit var apiRepository: ApiRepository

    @Mock
    private lateinit var view: MainUserListView

    private lateinit var viewModel: MainListTvViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        viewModel = MainListTvViewModel()
    }

    @Test
    fun testSetup() {
        runBlocking {
            val pages = 1
            val tag = "DiscoverTv"
            val supportedType = MainListTvViewModel.SupportedType.DISCOVER
            val path = File("tests/json")
            val returnedResults = ApiRepository.ReturnedResults(
                getResponse(path, "discover_tv_desc_popularity_sort.json"),
                null,
                true
            )

            Mockito.`when`(
                apiRepository.doRequestAndReturnJSON(
                    GetMovies.getAllGenre(),
                    "GetAllTvGenre",
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
                    GetTVShows.getList(
                        supportedType,
                        pages
                    ),
                    "${tag}Pages$pages",
                    Priority.LOW
                )
            ).thenReturn(GlobalScope.async { returnedResults })

            viewModel.setup(apiRepository, supportedType, pages, tag, view, TestCoroutineContextProvider())

            Mockito.verify(view).onStart()
            Mockito.verify(view).onLoadFinished()
            Mockito.verify(view).onLoadSuccess(viewModel)

        }
    }
}