package vip.mystery0.xhu.timetable.platform

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings

class AndroidAppInfo(
    private val context: Context,
    private val versionName: String,
    private val versionCode: Int,
) : AppInfo {

    @SuppressLint("HardwareIds")
    override fun deviceId(): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return "android-$androidId"
    }

    override fun versionName(): String = versionName

    override fun versionCode(): String = versionCode.toString()
}
