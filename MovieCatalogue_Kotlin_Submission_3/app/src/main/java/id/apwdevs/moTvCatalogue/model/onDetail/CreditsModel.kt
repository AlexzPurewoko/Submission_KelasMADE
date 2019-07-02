package id.apwdevs.moTvCatalogue.model.onDetail

import id.apwdevs.moTvCatalogue.model.ResettableItem

data class CreditsModel(
    val id: Int,
    val allCasts: MutableList<CastModel>,
    val allCrew: MutableList<CrewModel>
) : ResettableItem {
    override fun onReset() {
        allCasts.forEach {
            it.onReset()
        }
        allCrew.forEach {
            it.onReset()
        }
    }
}

data class CastModel(
    val castId: Int,
    val asCharacter: String,
    val creditId: String,
    val gender: Int?,
    val id: Int,
    var name: CharSequence,
    val order: Int,
    val profilePath: String?
) : ResettableItem {
    override fun onReset() {
        name = name.toString()
    }
}

data class CrewModel(
    val job: String,
    val creditId: String,
    val gender: Int?,
    val id: Int,
    var name: CharSequence,
    val department: String,
    val profilePath: String?
) : ResettableItem {
    override fun onReset() {
        name = name.toString()
    }

}