package vip.mystery0.xhu.timetable.shared.domain.exception

/**
 * Thrown when a write operation is attempted while the app is offline.
 *
 * Product rule: offline write is forbidden; callers should prompt the user to retry when online.
 */
class OfflineWriteException(
    message: String = "Offline write is forbidden",
) : IllegalStateException(message)

