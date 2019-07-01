package id.apwdevs.moTvCatalogue.model

data class OtherAboutTVModel(
    val sParser: String
) {
    init {
        setAll(sParser)
    }

    internal var originalLanguage: String? = null
    internal var tvStatus: String? = null
    internal var longTvRuntime: String? = null
    internal var tvGenres: String? = null
    internal var type: String? = null

    private fun setAll(sParser: String) {
        val allComponents = sParser.split("[|]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        this.tvStatus = allComponents[0]
        this.type = allComponents[1]
        this.originalLanguage = allComponents[2]
        this.longTvRuntime = allComponents[3]
        this.tvGenres = allComponents[4]
    }
}