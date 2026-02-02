package vip.mystery0.xhu.timetable.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> =
    fold(onSuccess = transform, onFailure = { Result.failure(it) })

fun <T> Flow<T>.asResult(): Flow<Result<T>> =
    map<T, Result<T>> { Result.success(it) }
        .catch { e ->
            if (e is CancellationException) throw e
            emit(Result.failure(e))
        }
