package id.apwdevs.app.favoritedisplayer.plugin

import androidx.fragment.app.Fragment

interface OnSelectedFragment {
    fun start(fragment: Fragment, position: Int)
}
