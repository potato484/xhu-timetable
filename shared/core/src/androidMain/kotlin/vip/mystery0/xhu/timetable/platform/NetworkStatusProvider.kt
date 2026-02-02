package vip.mystery0.xhu.timetable.platform

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class AndroidNetworkStatusProvider(
    private val context: Context,
) : NetworkStatusProvider {
    override fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
