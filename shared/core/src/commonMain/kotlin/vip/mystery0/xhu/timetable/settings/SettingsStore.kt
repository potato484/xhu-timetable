package vip.mystery0.xhu.timetable.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

sealed interface SettingScope {
    data object Global : SettingScope
    data class Student(val studentId: String) : SettingScope
    data class Partition(val studentId: String, val termYear: Int, val termIndex: Int) : SettingScope

    fun toScopeType(): String = when (this) {
        Global -> "GLOBAL"
        is Student -> "STUDENT"
        is Partition -> "PARTITION"
    }

    fun toScopeId(): String = when (this) {
        Global -> ""
        is Student -> studentId
        is Partition -> "$studentId:$termYear:$termIndex"
    }
}

interface SettingCodec<T> {
    fun encode(value: T): String
    fun decode(raw: String): T
}

object StringCodec : SettingCodec<String> {
    override fun encode(value: String): String = value
    override fun decode(raw: String): String = raw
}

object BooleanCodec : SettingCodec<Boolean> {
    override fun encode(value: Boolean): String = value.toString()
    override fun decode(raw: String): Boolean = raw.toBooleanStrictOrNull() ?: false
}

object IntCodec : SettingCodec<Int> {
    override fun encode(value: Int): String = value.toString()
    override fun decode(raw: String): Int = raw.toIntOrNull() ?: 0
}

object LongCodec : SettingCodec<Long> {
    override fun encode(value: Long): String = value.toString()
    override fun decode(raw: String): Long = raw.toLongOrNull() ?: 0L
}

object FloatCodec : SettingCodec<Float> {
    override fun encode(value: Float): String = value.toString()
    override fun decode(raw: String): Float = raw.toFloatOrNull() ?: 0f
}

object LocalDateNullableCodec : SettingCodec<LocalDate?> {
    override fun encode(value: LocalDate?): String = value?.toString().orEmpty()

    override fun decode(raw: String): LocalDate? = raw
        .takeIf { it.isNotBlank() }
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
}

data class SettingKey<T>(
    val scope: SettingScope,
    val name: String,
    val codec: SettingCodec<T>,
    val defaultValue: T,
)

interface SettingsStore {
    fun <T> observe(key: SettingKey<T>): Flow<T>
    suspend fun <T> get(key: SettingKey<T>): T
    suspend fun <T> set(key: SettingKey<T>, value: T)
    suspend fun <T> remove(key: SettingKey<T>)
    suspend fun clearScope(scope: SettingScope)
}
