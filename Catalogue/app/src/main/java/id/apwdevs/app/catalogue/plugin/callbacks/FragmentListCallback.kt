package id.apwdevs.app.catalogue.plugin.callbacks

import androidx.fragment.app.Fragment
import id.apwdevs.app.catalogue.plugin.PublicConfig

interface FragmentListCallback {
    fun onFragmentChange(newFragment: Fragment, fragmentType: PublicConfig.ContentDisplayType)
}