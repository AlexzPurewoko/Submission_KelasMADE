package id.apwdevs.app.favoritedisplayer.model.onDetail

import com.google.gson.annotations.SerializedName
import id.apwdevs.app.favoritedisplayer.model.ClassResponse

data class SocmedIDModel(

    @SerializedName("facebook_id")
    val facebookId: String?,

    @SerializedName("instagram_id")
    val instagramId: String?,

    @SerializedName("twitter_id")
    val twitterId: String?,


    @SerializedName("id")
    val id: Int
) : ClassResponse