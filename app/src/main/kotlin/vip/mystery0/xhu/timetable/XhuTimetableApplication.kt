package vip.mystery0.xhu.timetable

import android.app.Application
import android.webkit.WebSettings
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import vip.mystery0.xhu.timetable.db.AtomicTransaction
import vip.mystery0.xhu.timetable.db.SqlDelightAtomicTransaction
import vip.mystery0.xhu.timetable.di.coreModule
import vip.mystery0.xhu.timetable.di.platformCoreModule
import vip.mystery0.xhu.timetable.model.AccountContext
import vip.mystery0.xhu.timetable.platform.AndroidAppInfo
import vip.mystery0.xhu.timetable.platform.AppInfo
import vip.mystery0.xhu.timetable.platform.ioDispatcher
import vip.mystery0.xhu.timetable.shared.database.DriverFactory
import vip.mystery0.xhu.timetable.shared.database.SqlDelightSettingsStore
import vip.mystery0.xhu.timetable.shared.database.XhuTimetableDatabase
import vip.mystery0.xhu.timetable.shared.domain.di.domainModule
import vip.mystery0.xhu.timetable.shared.domain.repository.UserRepository
import vip.mystery0.xhu.timetable.shared.network.di.ACCOUNT_CONTEXT_PROVIDER_QUALIFIER
import vip.mystery0.xhu.timetable.shared.network.di.ON_UNAUTHORIZED_DEBOUNCED_QUALIFIER
import vip.mystery0.xhu.timetable.shared.network.di.USER_AGENT_QUALIFIER
import vip.mystery0.xhu.timetable.shared.network.di.networkModule
import vip.mystery0.xhu.timetable.shared.ui.auth.AuthEventBus
import vip.mystery0.xhu.timetable.shared.ui.di.uiModule
import vip.mystery0.xhu.timetable.settings.SettingsStore
import vip.mystery0.xhu.timetable.notification.initNotificationChannels
import vip.mystery0.xhu.timetable.notification.NotifyScheduler

class XhuTimetableApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initNotificationChannels(this)

        startKoin {
            androidContext(this@XhuTimetableApplication)
            modules(
                module {
                    single { DriverFactory(get()) }
                    single<SqlDriver> {
                        val driver = get<DriverFactory>().createDriver()
                        try {
                            driver.execute(null, "PRAGMA journal_mode=WAL", 0)
                        } catch (_: Exception) {
                            // WAL mode is optional; fallback to default journal mode
                        }
                        driver
                    }
                    single<XhuTimetableDatabase> { XhuTimetableDatabase(get()) }
                    single<AtomicTransaction> { SqlDelightAtomicTransaction(get(), ioDispatcher) }
                    single<SettingsStore> { SqlDelightSettingsStore(get(), Dispatchers.IO) }
                    single<String>(named(USER_AGENT_QUALIFIER)) {
                        runCatching { WebSettings.getDefaultUserAgent(get()) }.getOrElse { "" }
                    }

                    single<AppInfo> {
                        AndroidAppInfo(
                            context = get(),
                            versionName = BuildConfig.VERSION_NAME,
                            versionCode = BuildConfig.VERSION_CODE,
                        )
                    }
                    single<suspend () -> AccountContext?>(named(ACCOUNT_CONTEXT_PROVIDER_QUALIFIER)) {
                        suspend { get<UserRepository>().getCurrentAccount() }
                    }
                    single<suspend () -> Unit>(named(ON_UNAUTHORIZED_DEBOUNCED_QUALIFIER)) {
                        suspend { AuthEventBus.emitSessionExpired() }
                    }
                },
                coreModule,
                platformCoreModule,
                networkModule,
                domainModule,
                uiModule,
            )
        }
    }
}
