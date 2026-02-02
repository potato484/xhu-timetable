package vip.mystery0.xhu.timetable.di

import org.koin.core.module.Module
import org.koin.dsl.module
import vip.mystery0.xhu.timetable.platform.AppFileStorage
import vip.mystery0.xhu.timetable.platform.IosAppFileStorage
import vip.mystery0.xhu.timetable.platform.IosNetworkStatusProvider
import vip.mystery0.xhu.timetable.platform.NetworkStatusProvider

actual val platformCoreModule: Module = module {
    single<NetworkStatusProvider> { IosNetworkStatusProvider() }
    single<AppFileStorage> { IosAppFileStorage() }
}
