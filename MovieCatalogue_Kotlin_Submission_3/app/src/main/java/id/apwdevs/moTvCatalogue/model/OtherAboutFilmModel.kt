package id.apwdevs.moTvCatalogue.model

import java.util.*

data class OtherAboutFilmModel(
    val sParser: String
) {
    init {
        setAll(sParser)
    }

    internal var longMovieRuntime: String? = null
    internal var originalLanguage: String? = null
    internal var movieRevenue: String? = null
    internal var movieBudget: String? = null
    internal var movieGenres: String? = null
    var listTopBilledCast: ArrayList<FilmTopBilledCastModel> = ArrayList()

    private fun setAll(sParser: String) {
        val allComponents = sParser.split("[|]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        this.originalLanguage = allComponents[0]
        this.longMovieRuntime = allComponents[1]
        this.movieBudget = allComponents[2]
        this.movieRevenue = allComponents[3]
        this.movieGenres = allComponents[4]
    }

    fun setListTopBilledCast(strList: String) {
        if (listTopBilledCast.size > 0)
            listTopBilledCast.clear()
        val buff = StringBuilder()
        var buff2: String? = null
        for (x in 0 until strList.length) {
            when (strList[x]) {
                '=' -> {
                    buff2 = buff.toString()
                    buff.delete(0, buff.length)
                }
                ',' -> {
                    if (buff2 != null) {
                        listTopBilledCast.add(FilmTopBilledCastModel(buff2, buff.toString()))
                        buff2 = null
                    }
                    buff.delete(0, buff.length)
                }
                else -> buff.append(strList[x])
            }
        }
        listTopBilledCast.add(FilmTopBilledCastModel(buff2, buff.toString()))
    }
}