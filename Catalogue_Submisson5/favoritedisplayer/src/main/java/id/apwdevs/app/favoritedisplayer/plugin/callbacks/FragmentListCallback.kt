package id.apwdevs.app.catalogue.plugin.callbacks

import androidx.fragment.app.Fragment
import id.apwdevs.app.catalogue.plugin.PublicContract

interface FragmentListCallback {
    fun onFragmentChange(newFragment: Fragment, fragmentType: PublicContract.ContentDisplayType)
}