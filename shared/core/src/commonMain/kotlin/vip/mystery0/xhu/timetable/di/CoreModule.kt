package vip.mystery0.xhu.timetable.di

import org.koin.core.module.Module
import org.koin.dsl.module
import vip.mystery0.xhu.timetable.crypto.login.LoginEncryptor
import vip.mystery0.xhu.timetable.crypto.login.whyoleg.WhyolegLoginEncryptor
import vip.mystery0.xhu.timetable.platform.ClockProvider
import vip.mystery0.xhu.timetable.platform.SystemClockProvider

val coreModule: Module = module {
    single<LoginEncryptor> { WhyolegLoginEncryptor() }
    single<ClockProvider> { SystemClockProvider() }
}

expect val platformCoreModule: Module
