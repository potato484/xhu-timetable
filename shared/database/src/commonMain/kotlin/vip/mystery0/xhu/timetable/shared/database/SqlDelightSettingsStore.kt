package vip.mystery0.xhu.timetable.shared.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import vip.mystery0.xhu.timetable.settings.SettingKey
import vip.mystery0.xhu.timetable.settings.SettingScope
import vip.mystery0.xhu.timetable.settings.SettingsStore

class SqlDelightSettingsStore(
    private val database: XhuTimetableDatabase,
    private val dispatcher: CoroutineDispatcher,
) : SettingsStore {

    private val queries get() = database.schemaQueries

    override fun <T> observe(key: SettingKey<T>): Flow<T> {
        return queries.selectSetting(
            scope = key.scope.toScopeType(),
            scopeId = key.scope.toScopeId(),
            name = key.name,
        ).asFlow()
            .mapToOneOrNull(dispatcher)
            .map { raw ->
                if (raw != null) {
                    key.codec.decode(raw)
                } else {
                    key.defaultValue
                }
            }
    }

    override suspend fun <T> get(key: SettingKey<T>): T = withContext(dispatcher) {
        val raw = queries.selectSetting(
            scope = key.scope.toScopeType(),
            scopeId = key.scope.toScopeId(),
            name = key.name,
        ).executeAsOneOrNull()
        if (raw != null) {
            key.codec.decode(raw)
        } else {
            key.defaultValue
        }
    }

    override suspend fun <T> set(key: SettingKey<T>, value: T) = withContext(dispatcher) {
        queries.upsertSetting(
            scope = key.scope.toScopeType(),
            scopeId = key.scope.toScopeId(),
            name = key.name,
            value = key.codec.encode(value),
            updatedAt = Clock.System.now().toEpochMilliseconds(),
        )
    }

    override suspend fun <T> remove(key: SettingKey<T>) = withContext(dispatcher) {
        queries.deleteSetting(
            scope = key.scope.toScopeType(),
            scopeId = key.scope.toScopeId(),
            name = key.name,
        )
    }

    override suspend fun clearScope(scope: SettingScope) = withContext(dispatcher) {
        queries.deleteSettingsByScope(
            scope = scope.toScopeType(),
            scopeId = scope.toScopeId(),
        )
    }
}
