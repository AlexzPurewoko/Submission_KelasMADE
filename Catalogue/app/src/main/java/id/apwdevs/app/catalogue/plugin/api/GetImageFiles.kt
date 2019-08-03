package id.apwdevs.app.catalogue.plugin.api

object GetImageFiles {
    val LIST_SUPPORTED_WSIZES = mutableListOf(
        92,
        154,
        185,
        342,
        500,
        780
    )

    fun getImg(width: Int, link: String): String {
        var selectedW: String? = null
        for (wSize in LIST_SUPPORTED_WSIZES) {
            if (width <= wSize) {
                selectedW = "w$wSize"
                break
            }
        }
        if (selectedW == null)
            selectedW = "original"
        return "https://image.tmdb.org/t/p/$selectedW$link"
    }

}