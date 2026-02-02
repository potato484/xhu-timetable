package vip.mystery0.xhu.timetable.shared.domain.di

import org.koin.core.module.Module
import org.koin.dsl.module
import vip.mystery0.xhu.timetable.platform.NetworkStatusProvider
import vip.mystery0.xhu.timetable.platform.ioDispatcher
import vip.mystery0.xhu.timetable.shared.domain.crypto.TokenCipher
import vip.mystery0.xhu.timetable.shared.domain.repository.BackgroundRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.BackgroundRepositoryImpl
import vip.mystery0.xhu.timetable.shared.domain.repository.SchoolInfoRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.SchoolInfoRepositoryImpl
import vip.mystery0.xhu.timetable.shared.domain.repository.CourseColorRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.CourseColorRepositoryImpl
import vip.mystery0.xhu.timetable.shared.domain.repository.CourseRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.CourseRepositoryImpl
import vip.mystery0.xhu.timetable.shared.domain.repository.CustomCourseRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.CustomCourseRepositoryImpl
import vip.mystery0.xhu.timetable.shared.domain.repository.CustomThingRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.CustomThingRepositoryImpl
import vip.mystery0.xhu.timetable.shared.domain.repository.ExamRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.ExamRepositoryImpl
import vip.mystery0.xhu.timetable.shared.domain.repository.NoticeRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.NoticeRepositoryImpl
import vip.mystery0.xhu.timetable.shared.domain.repository.ScoreRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.ScoreRepositoryImpl
import vip.mystery0.xhu.timetable.shared.domain.repository.SettingsRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.SettingsRepositoryImpl
import vip.mystery0.xhu.timetable.shared.domain.repository.TermRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.TermRepositoryImpl
import vip.mystery0.xhu.timetable.shared.domain.repository.UserRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.UserRepositoryImpl
import vip.mystery0.xhu.timetable.shared.domain.usecase.AggregationUseCase
import vip.mystery0.xhu.timetable.shared.domain.usecase.AggregationUseCaseImpl

val domainModule: Module = module {
    single { TokenCipher() }
    single<UserRepository> { UserRepositoryImpl(get(), get(), get(), get()) }
    single<TermRepository> { TermRepositoryImpl(get(), get(), get(), get()) }
    single<CourseRepository> { CourseRepositoryImpl(get(), get(), get()) }
    single<ExamRepository> { ExamRepositoryImpl(get(), get()) }
    single<ScoreRepository> { ScoreRepositoryImpl(get(), get()) }
    single<CustomCourseRepository> {
        val networkStatus: NetworkStatusProvider = get()
        CustomCourseRepositoryImpl(get(), get(), get(), networkStatus::isOnline)
    }
    single<CustomThingRepository> {
        val networkStatus: NetworkStatusProvider = get()
        CustomThingRepositoryImpl(get(), get(), get(), networkStatus::isOnline)
    }
    single<AggregationUseCase> { AggregationUseCaseImpl(get(), get()) }

    // New repositories
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<CourseColorRepository> { CourseColorRepositoryImpl(get(), ioDispatcher) }
    single<NoticeRepository> { NoticeRepositoryImpl(get(), get(), get(), ioDispatcher) }
    single<BackgroundRepository> { BackgroundRepositoryImpl(get(), get(), get(), ioDispatcher) }
    single<SchoolInfoRepository> { SchoolInfoRepositoryImpl(get(), ioDispatcher) }
}
