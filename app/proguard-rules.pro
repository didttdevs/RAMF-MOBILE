# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep data classes for API models
-keepclassmembers class com.example.rafapp.models.** { *; }

# Keep Retrofit service interfaces
-keep interface com.example.rafapp.network.** { *; }

# Keep ViewBinding classes
-keep class com.example.rafapp.databinding.** { *; }

# Keep ViewModel classes
-keep class com.example.rafapp.viewmodels.** { *; }

# Gson specific rules
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Retrofit specific rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp specific rules
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# MPAndroidChart rules
-keep class com.github.mikephil.charting.** { *; }

# Material Components rules
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# AndroidX rules
-keep class androidx.** { *; }
-dontwarn androidx.**

# Keep application class
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Security crypto rules
-keep class androidx.security.crypto.** { *; }

# Google Play Services rules
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Remove logs in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Optimization
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
-optimizationpasses 5
-allowaccessmodification
-dontpreverify