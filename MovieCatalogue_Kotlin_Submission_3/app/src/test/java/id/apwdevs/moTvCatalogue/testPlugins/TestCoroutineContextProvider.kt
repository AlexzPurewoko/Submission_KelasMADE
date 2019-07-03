package id.apwdevs.moTvCatalogue.testPlugins

import id.apwdevs.moTvCatalogue.plugin.CoroutineContextProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.coroutines.CoroutineContext

class TestCoroutineContextProvider : CoroutineContextProvider() {
    @ExperimentalCoroutinesApi
    override val main: CoroutineContext
        get() = Dispatchers.Unconfined
}