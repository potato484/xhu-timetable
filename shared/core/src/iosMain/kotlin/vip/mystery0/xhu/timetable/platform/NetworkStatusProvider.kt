package vip.mystery0.xhu.timetable.platform

class IosNetworkStatusProvider : NetworkStatusProvider {
    override fun isOnline(): Boolean {
        // TODO: Implement iOS network status check using NWPathMonitor
        return true
    }
}
