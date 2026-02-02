package vip.mystery0.xhu.timetable.platform

interface AppFileStorage {
    val pictureDir: String
    val downloadDir: String
    val cacheDownloadDir: String
    val documentsDir: String
    val customImageDir: String

    fun clearDownloadDir()
    fun ensureDir(path: String): Boolean
}
