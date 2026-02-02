package vip.mystery0.xhu.timetable.shared.ui.di

import org.koin.core.module.Module
import org.koin.dsl.module
import vip.mystery0.xhu.timetable.shared.ui.account.AccountViewModel
import vip.mystery0.xhu.timetable.shared.ui.auth.LoginViewModel
import vip.mystery0.xhu.timetable.shared.ui.background.BackgroundViewModel
import vip.mystery0.xhu.timetable.shared.ui.base.NetworkStatusViewModel
import vip.mystery0.xhu.timetable.shared.ui.custom.CustomCourseEditViewModel
import vip.mystery0.xhu.timetable.shared.ui.custom.CustomThingEditViewModel
import vip.mystery0.xhu.timetable.shared.ui.custom.CustomViewModel
import vip.mystery0.xhu.timetable.shared.ui.examscore.ExamScoreViewModel
import vip.mystery0.xhu.timetable.shared.ui.feedback.FeedbackViewModel
import vip.mystery0.xhu.timetable.shared.ui.init.InitViewModel
import vip.mystery0.xhu.timetable.shared.ui.init.SplashImageViewModel
import vip.mystery0.xhu.timetable.shared.ui.notice.NoticeViewModel
import vip.mystery0.xhu.timetable.shared.ui.schoolinfo.FreeRoomViewModel
import vip.mystery0.xhu.timetable.shared.ui.schoolinfo.SchoolCalendarViewModel
import vip.mystery0.xhu.timetable.shared.ui.schoolinfo.SchoolTimetableViewModel
import vip.mystery0.xhu.timetable.shared.ui.settings.ClassSettingsViewModel
import vip.mystery0.xhu.timetable.shared.ui.settings.CustomUiViewModel
import vip.mystery0.xhu.timetable.shared.ui.settings.SettingsViewModel
import vip.mystery0.xhu.timetable.shared.ui.timetable.TimetableViewModel
import vip.mystery0.xhu.timetable.shared.ui.coursecolor.CourseColorViewModel

val uiModule: Module = module {
    single { NetworkStatusViewModel(get()) }
    factory { LoginViewModel(get(), get(), get()) }
    factory { AccountViewModel(get()) }
    factory { TimetableViewModel(get(), get(), get(), get(), get(), get(), get()) }
    factory { ExamScoreViewModel(get(), get(), get(), get()) }
    factory { CustomViewModel(get(), get(), get(), get()) }
    factory { CustomCourseEditViewModel(get(), get(), get()) }
    factory { CustomThingEditViewModel(get(), get()) }
    factory { SettingsViewModel(get()) }
    factory { ClassSettingsViewModel(get(), get(), get()) }
    factory { CustomUiViewModel(get(), get(), get()) }
    factory { InitViewModel(get()) }
    factory { SplashImageViewModel() }
    factory { NoticeViewModel(get()) }
    factory { BackgroundViewModel(get()) }
    factory { FeedbackViewModel() }
    factory { SchoolCalendarViewModel(get()) }
    factory { SchoolTimetableViewModel(get()) }
    factory { FreeRoomViewModel(get()) }
    factory { CourseColorViewModel(get(), get(), get(), get(), get()) }
}
