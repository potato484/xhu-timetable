package vip.mystery0.xhu.timetable.shared.domain.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import vip.mystery0.xhu.timetable.settings.SelectedBackground
import vip.mystery0.xhu.timetable.shared.database.XhuTimetableDatabase
import vip.mystery0.xhu.timetable.shared.network.BackgroundApi

data class Background(
    val backgroundId: Long,
    val resourceId: Long,
    val thumbnailUrl: String,
    val imageUrl: String,
)

interface BackgroundRepository {
    fun observeBackgrounds(): Flow<List<Background>>
    fun observeSelectedBackground(): Flow<SelectedBackground>
    suspend fun syncBackgrounds(): Result<Unit>
    suspend fun setSelectedBackground(selection: SelectedBackground)
}

class BackgroundRepositoryImpl(
    private val backgroundApi: BackgroundApi,
    private val database: XhuTimetableDatabase,
    private val settingsRepository: SettingsRepository,
    private val dispatcher: CoroutineContext,
) : BackgroundRepository {

    private val queries get() = database.schemaQueries

    override fun observeBackgrounds(): Flow<List<Background>> {
        return queries.selectAllBackgrounds()
            .asFlow()
            .mapToList(dispatcher)
            .map { dbBackgrounds ->
                dbBackgrounds.map { it.toDomain() }
            }
    }

    override fun observeSelectedBackground(): Flow<SelectedBackground> {
        return settingsRepository.observeBackgroundSelection()
    }

    override suspend fun syncBackgrounds(): Result<Unit> = withContext(dispatcher) {
        try {
            val backgrounds = backgroundApi.getBackgroundList()
            val now = Clock.System.now().toEpochMilliseconds()

            database.transaction {
                queries.deleteAllBackgrounds()
                backgrounds.forEach { bg ->
                    queries.upsertBackground(
                        backgroundId = bg.backgroundId,
                        resourceId = bg.resourceId,
                        thumbnailUrl = bg.thumbnailUrl,
                        imageUrl = bg.imageUrl,
                        updatedAt = now,
                    )
                }
            }
            Result.success(Unit)
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setSelectedBackground(selection: SelectedBackground) {
        settingsRepository.setBackgroundSelection(selection)
    }

    private fun vip.mystery0.xhu.timetable.shared.database.Background.toDomain(): Background {
        return Background(
            backgroundId = backgroundId,
            resourceId = resourceId,
            thumbnailUrl = thumbnailUrl,
            imageUrl = imageUrl,
        )
    }
}
