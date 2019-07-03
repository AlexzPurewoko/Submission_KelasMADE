package id.apwdevs.moTvCatalogue.testPlugins

import java.io.File
import java.io.FileInputStream


fun getResponse(path: File, jsonFileName: String): String {

    // read file from local
    val fileJSON = File(path, jsonFileName)
    val inputStream = FileInputStream(fileJSON.absolutePath)
    val response = String(inputStream.readBytes())
    inputStream.close()
    return response
}