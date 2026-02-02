package vip.mystery0.xhu.timetable.platform

import android.content.Context
import android.os.Environment
import java.io.File

class AndroidAppFileStorage(private val context: Context) : AppFileStorage {
    override val pictureDir: String
        get() = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath ?: ""

    override val downloadDir: String
        get() = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath ?: ""

    override val cacheDownloadDir: String
        get() = File(context.externalCacheDir, "update").absolutePath

    override val documentsDir: String
        get() = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath ?: ""

    override val customImageDir: String
        get() = File(pictureDir, "custom").absolutePath

    override fun clearDownloadDir() {
        File(downloadDir).deleteRecursively()
    }

    override fun ensureDir(path: String): Boolean {
        val dir = File(path)
        return dir.exists() || dir.mkdirs()
    }
}
