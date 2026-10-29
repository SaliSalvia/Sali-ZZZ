# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class com.salizzai.app.**$$serializer { *; }
-keepclassmembers class com.salizzai.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.salizzai.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Retrofit (just in case)
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Room
-keep class androidx.room.** { *; }
-keep class com.salizzai.app.data.db.** { *; }

# Markwon
-keep class io.noties.markwon.** { *; }
-keep class org.commonmark.** { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep app data classes
-keep class com.salizzai.app.data.model.** { *; }
