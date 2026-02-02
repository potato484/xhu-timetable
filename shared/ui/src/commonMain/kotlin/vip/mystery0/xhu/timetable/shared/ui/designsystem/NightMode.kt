package vip.mystery0.xhu.timetable.shared.ui.designsystem

enum class NightMode(
    val value: Int,
    val title: String
) {
    AUTO(0, "自动"),
    ON(1, "始终开启"),
    OFF(2, "始终关闭"),
    MATERIAL_YOU(3, "Material You"),
    ;

    companion object {
        fun fromValue(value: Int): NightMode = entries.find { it.value == value } ?: AUTO
        fun fromName(name: String): NightMode = entries.find { it.name == name } ?: AUTO
    }
}
