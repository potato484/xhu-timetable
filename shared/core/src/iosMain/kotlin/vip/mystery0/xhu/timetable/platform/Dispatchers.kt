package vip.mystery0.xhu.timetable.platform

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

actual val ioDispatcher: CoroutineContext = Dispatchers.Default
actual val mainDispatcher: CoroutineContext = Dispatchers.Main
