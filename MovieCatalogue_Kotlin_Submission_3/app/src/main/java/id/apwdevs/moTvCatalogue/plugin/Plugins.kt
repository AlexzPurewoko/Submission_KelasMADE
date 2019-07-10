package id.apwdevs.moTvCatalogue.plugin


fun jsonCheckAndGet(get: Any): Any? =
    if (get.toString() == "null") null
    else get