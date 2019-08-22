package id.apwdevs.app.catalogue

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import id.apwdevs.app.catalogue.model.onUserMain.MainDataItemResponse
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer
import id.apwdevs.app.catalogue.plugin.api.GetTVShows
import id.apwdevs.app.catalogue.repository.onUserMain.FragmentContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
        assertEquals("id.apwdevs.app.catalogue", appContext.packageName)

        GlobalScope.launch(Dispatchers.IO) {
            var progress = 0.0
            var isFinished = false

            val ooo = GetObjectFromServer.getInstance(appContext).getSynchronousObj(
                GetTVShows.getAllGenre(),
                MainDataItemResponse::class.java,
                "aaaaa",
                object : GetObjectFromServer.GetObjectFromServerCallback<MainDataItemResponse> {
                    override fun onSuccess(response: MainDataItemResponse) {
                        isFinished = true
                    }

                    override fun onFailed(retError: GetObjectFromServer.RetError) {
                        retError.cause?.printStackTrace()
                        isFinished = true
                    }

                    override fun onProgress(percent: Double) {
                        val currProgress =
                            percent * FragmentContentRepository.RES_OBJ_LOAD_FACTOR / FragmentContentRepository.MAX_FACTOR
                        //this@FragmentContentRepository.onProgress(currProgress - progress)
                        progress = currProgress
                    }

                }
            )

        }
        while (true) Thread.sleep(1000)
    }

    private suspend fun <T> loadFromInet(
        url: String,
        tag: String,
        objServer: GetObjectFromServer
    ): T? {
        var progress = 0.0
        var isFinished = false

        objServer.getObj(
            url,
            MainDataItemResponse::class.java as Class<T>,
            tag,
            object : GetObjectFromServer.GetObjectFromServerCallback<T> {
                override fun onSuccess(response: T) {
                    isFinished = true
                }

                override fun onFailed(retError: GetObjectFromServer.RetError) {
                    retError.cause?.printStackTrace()
                    isFinished = true
                }

                override fun onProgress(percent: Double) {
                    val currProgress =
                        percent * FragmentContentRepository.RES_OBJ_LOAD_FACTOR / FragmentContentRepository.MAX_FACTOR
                    //this@FragmentContentRepository.onProgress(currProgress - progress)
                    progress = currProgress
                }

            }
        )/*?.apply {
            if(this is MainDataItemResponse)
                contents?.forEach {
                    it.contentTypes = type.type
                }
        }*/
        return null
    }
}
