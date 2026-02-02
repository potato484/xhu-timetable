package vip.mystery0.xhu.timetable.platform

import platform.Foundation.*

class IosAppFileStorage : AppFileStorage {
    override val pictureDir: String
        get() = getEnsuredPath(NSDocumentDirectory, "Pictures")

    override val downloadDir: String
        get() = getEnsuredPath(NSDocumentDirectory, "Downloads")

    override val cacheDownloadDir: String
        get() = getEnsuredPath(NSCachesDirectory, "update")

    override val documentsDir: String
        get() = getPath(NSDocumentDirectory)

    override val customImageDir: String
        get() = getEnsuredPath(NSDocumentDirectory, "Pictures", "custom")

    override fun clearDownloadDir() {
        val fileManager = NSFileManager.defaultManager
        fileManager.removeItemAtPath(downloadDir, null)
    }

    override fun ensureDir(path: String): Boolean {
        val fileManager = NSFileManager.defaultManager
        return if (!fileManager.fileExistsAtPath(path)) {
            fileManager.createDirectoryAtPath(path, true, null, null)
        } else {
            true
        }
    }

    private fun getPath(directory: NSSearchPathDirectory): String {
        val paths = NSSearchPathForDirectoriesInDomains(directory, NSUserDomainMask, true)
        return (paths.firstOrNull() as? String) ?: ""
    }

    private fun getEnsuredPath(directory: NSSearchPathDirectory, vararg components: String): String {
        var currentPath = getPath(directory)
        val fileManager = NSFileManager.defaultManager

        if (!fileManager.fileExistsAtPath(currentPath)) {
            fileManager.createDirectoryAtPath(currentPath, true, null, null)
        }

        components.forEach { component ->
            currentPath = "$currentPath/$component"
            if (!fileManager.fileExistsAtPath(currentPath)) {
                fileManager.createDirectoryAtPath(currentPath, true, null, null)
            }
        }
        return currentPath
    }
}
