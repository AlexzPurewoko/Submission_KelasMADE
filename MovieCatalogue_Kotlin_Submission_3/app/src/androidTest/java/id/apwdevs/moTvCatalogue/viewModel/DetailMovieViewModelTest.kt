package id.apwdevs.moTvCatalogue.viewModel

import android.content.Context
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.androidnetworking.AndroidNetworking
import id.apwdevs.moTvCatalogue.activities.MainActivity
import id.apwdevs.moTvCatalogue.plugin.ApiRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetailMovieViewModelTest {

    private lateinit var mContext: Context

    private lateinit var apiRepository: ApiRepository

    private lateinit var viewModel: DetailMovieViewModel

    @JvmField
    var activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {

    }

    @Test
    fun testGetSocmedID() {
        AndroidNetworking.initialize(activityRule.activity.applicationContext)
        viewModel = DetailMovieViewModel()
        GlobalScope.launch {
            val results = viewModel.setListSocmedId(apiRepository, 301528).await()
            assertEquals(null, results)

        }
    }
}