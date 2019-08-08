package id.apwdevs.app.favoritedisplayer.activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import id.apwdevs.app.favoritedisplayer.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    val uri = Uri.Builder().apply {
        authority("id.apwdevs.app.catalogue")
        appendPath("favorite")
    }.build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlobalScope.launch(Dispatchers.IO) {

            val FAVORITE_TABLE = "favorite"
            val GENRE_TABLE = "genres"
            val AUTHORITY = "id.apwdevs.app.catalogue"
            val fg = contentResolver.query(Uri.Builder().apply {
                scheme("content")
                authority(AUTHORITY)
                appendPath(FAVORITE_TABLE)
            }.build(), null, null, null, null)
            val res = fg
            Log.d("HELKDLE", "tttt $res")
            fg?.close()
        }
    }

    interface GetFromHostActivity {
        fun getListMode(): Int
    }
}
