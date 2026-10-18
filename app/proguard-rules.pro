# ============================================================
# 项目自身的 keep 规则
# 三方库（Room / Koin / kotlinx-serialization / Coil）自带的 consumer 规则
# 已经覆盖了大部分场景，这里只补一些在 Release 包里容易被裁掉的入口。
# ============================================================

# ---- 调试信息，便于线上崩溃定位 ----
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- 泛型 / 注解 / 枚举（Room、序列化都依赖）----
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod
-keepclassmembers enum * { *; }

# ---- Room ----
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# ---- Koin：构造注入靠反射找构造函数 ----
-keep class org.koin.** { *; }
-keep class com.mnn.cameranote.di.** { *; }
-keepclassmembers class com.mnn.cameranote.**ViewModel { <init>(...); }

# ---- Navigation 类型安全路由使用 kotlinx.serialization ----
-keepclassmembers @kotlinx.serialization.Serializable class com.mnn.cameranote.navigation.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.mnn.cameranote.navigation.**$$serializer { *; }

# ---- CameraX 的部分实现由反射加载 ----
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**
