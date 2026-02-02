package vip.mystery0.xhu.timetable.shared.domain.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import vip.mystery0.xhu.timetable.shared.database.XhuTimetableDatabase
import vip.mystery0.xhu.timetable.shared.network.NoticeApi
import vip.mystery0.xhu.timetable.shared.network.model.NoticeAction as NetworkNoticeAction

data class Notice(
    val noticeId: Long,
    val title: String,
    val content: String,
    val actions: List<NoticeAction>,
    val released: Boolean,
    val createTime: Instant,
    val updateTime: Instant,
)

data class NoticeAction(
    val text: String,
    val actionType: NoticeActionType,
    val metadata: String,
)

enum class NoticeActionType {
    COPY,
    OPEN_URI,
    UNKNOWN,
}

interface NoticeRepository {
    fun observeNotices(): Flow<List<Notice>>
    fun observeHasUnread(): Flow<Boolean>
    suspend fun refreshNotices(pageIndex: Int = 0, pageSize: Int = 20): Result<Boolean>
    suspend fun checkHasNew(): Result<Boolean>
    suspend fun markAllAsRead()
}

class NoticeRepositoryImpl(
    private val noticeApi: NoticeApi,
    private val database: XhuTimetableDatabase,
    private val settingsRepository: SettingsRepository,
    private val dispatcher: CoroutineContext,
) : NoticeRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val queries get() = database.schemaQueries

    override fun observeNotices(): Flow<List<Notice>> {
        return queries.selectAllNotices()
            .asFlow()
            .mapToList(dispatcher)
            .map { dbNotices ->
                dbNotices.map { it.toDomain() }
            }
    }

    override fun observeHasUnread(): Flow<Boolean> {
        return combine(
            queries.selectLatestNoticeId().asFlow().mapToOneOrNull(dispatcher).map { it ?: 0L },
            settingsRepository.observeNoticeLastReadId(),
        ) { latestId, lastReadId ->
            latestId > lastReadId
        }
    }

    override suspend fun refreshNotices(pageIndex: Int, pageSize: Int): Result<Boolean> = withContext(dispatcher) {
        try {
            val response = noticeApi.getNoticeList(index = pageIndex, size = pageSize)

            database.transaction {
                response.items.forEach { notice ->
                    queries.upsertNotice(
                        noticeId = notice.noticeId,
                        title = notice.title,
                        content = notice.content,
                        actionsJson = json.encodeToString(notice.actions),
                        released = if (notice.released) 1L else 0L,
                        createTime = parseTime(notice.createTime),
                        updateTime = parseTime(notice.updateTime),
                    )
                }
            }

            // Update last seen id only if this is the first page (newest notices)
            if (pageIndex == 0) {
                val maxId = response.items.maxOfOrNull { it.noticeId } ?: 0L
                settingsRepository.setNoticeLastSeenId(maxOf(maxId, 0L))
            }

            Result.success(response.hasNext)
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkHasNew(): Result<Boolean> = withContext(dispatcher) {
        try {
            val lastSeenId = queries.selectLatestNoticeId().executeAsOne()
            Result.success(noticeApi.checkHasNew(lastSeenId))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead() = withContext(dispatcher) {
        val latestId = queries.selectLatestNoticeId().executeAsOne()
        settingsRepository.setNoticeLastReadId(latestId)
    }

    private fun vip.mystery0.xhu.timetable.shared.database.Notice.toDomain(): Notice {
        val actionsList = try {
            json.decodeFromString<List<NetworkNoticeAction>>(actionsJson).map { networkAction ->
                NoticeAction(
                    text = networkAction.text,
                    actionType = when (networkAction.actionType.uppercase()) {
                        "COPY" -> NoticeActionType.COPY
                        "OPEN_URI" -> NoticeActionType.OPEN_URI
                        else -> NoticeActionType.UNKNOWN
                    },
                    metadata = networkAction.metadata,
                )
            }
        } catch (_: Exception) {
            emptyList()
        }

        return Notice(
            noticeId = noticeId,
            title = title,
            content = content,
            actions = actionsList,
            released = released == 1L,
            createTime = Instant.fromEpochMilliseconds(createTime),
            updateTime = Instant.fromEpochMilliseconds(updateTime),
        )
    }

    private fun parseTime(timeStr: String): Long {
        return try {
            Instant.parse(timeStr).toEpochMilliseconds()
        } catch (_: Exception) {
            Clock.System.now().toEpochMilliseconds()
        }
    }
}
