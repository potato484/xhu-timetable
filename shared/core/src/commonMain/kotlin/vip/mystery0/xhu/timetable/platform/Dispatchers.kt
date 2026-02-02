package vip.mystery0.xhu.timetable.platform

import kotlin.coroutines.CoroutineContext

expect val ioDispatcher: CoroutineContext
expect val mainDispatcher: CoroutineContext
