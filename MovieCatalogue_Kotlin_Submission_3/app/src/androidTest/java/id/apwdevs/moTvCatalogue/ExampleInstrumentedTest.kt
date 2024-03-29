package id.apwdevs.moTvCatalogue

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.androidnetworking.AndroidNetworking
import id.apwdevs.moTvCatalogue.plugin.api.ApiRepository
import id.apwdevs.moTvCatalogue.viewModel.DetailMovieViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        AndroidNetworking.initialize(appContext)
        val viewModel = DetailMovieViewModel()
        runBlocking {
            val results = viewModel.setListSocmedId(ApiRepository(), 301528).await()
            assertEquals(null, results)

        }
    }
}
