# ==============================================================================
# Quovex Android ProGuard / R8 Configuration Rules
# ==============================================================================

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# --- Retrofit & OkHttp ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# --- Gson / Model DTO Serialization ---
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.quovex.data.remote.dto.** { *; }
-keep class com.quovex.domain.model.** { *; }
-keep class com.quovex.domain.model.originals.** { *; }

# --- Room Database ---
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class com.quovex.data.local.entity.** { *; }
-keep class com.quovex.data.local.dao.** { *; }

# --- Dagger / Hilt ---
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# --- Firebase & Google Services ---
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# --- Compose ---
-keep class androidx.compose.** { *; }
