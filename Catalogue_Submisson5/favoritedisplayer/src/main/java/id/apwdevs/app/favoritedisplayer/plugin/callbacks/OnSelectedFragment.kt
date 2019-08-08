package id.apwdevs.app.catalogue.plugin.callbacks

import androidx.fragment.app.Fragment

interface OnSelectedFragment {
    fun start(fragment: Fragment, position: Int)
}
