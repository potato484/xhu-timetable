# ProGuard rules for XHU Timetable

# Keep application class
-keep class vip.mystery0.xhu.timetable.Application { *; }

# Keep data models
-keep class vip.mystery0.xhu.timetable.model.** { *; }
-keep class vip.mystery0.xhu.timetable.model.entity.** { *; }
-keep class vip.mystery0.xhu.timetable.model.request.** { *; }
-keep class vip.mystery0.xhu.timetable.model.response.** { *; }

# Keep Room entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Ktor
-dontwarn io.ktor.**

# MMKV
-keep class com.tencent.mmkv.** { *; }

# Coil
-dontwarn coil3.**
