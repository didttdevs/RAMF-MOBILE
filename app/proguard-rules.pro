# ============================================================================
# ProGuard/R8 Rules for RAF App - Producción
# ============================================================================
# Optimizado para release builds con máxima compatibilidad

# ============================================================================
# CONFIGURACIÓN GENERAL
# ============================================================================

# Mantener atributos importantes
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable
-keepattributes Exceptions

# Renombrar agresivamente para ofuscar mejor
-repackageclasses ''
-allowaccessmodification

# Optimizaciones
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
-optimizationpasses 5

# ============================================================================
# MODELOS DE DATOS (API Response Models)
# ============================================================================

# Mantener todas las clases de modelos - Gson las necesita
-keep class com.cocido.ramfapp.models.** { *; }
-keepclassmembers class com.cocido.ramfapp.models.** { *; }

# Mantener constructores de modelos
-keepclassmembers class com.cocido.ramfapp.models.** {
    <init>(...);
}

# ============================================================================
# NETWORK - Retrofit & OkHttp
# ============================================================================

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Mantener interfaces de servicios de red
-keep interface com.cocido.ramfapp.network.** { *; }
-keepclassmembers interface com.cocido.ramfapp.network.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# OkHttp Interceptors
-keep class com.cocido.ramfapp.network.interceptors.** { *; }

# ============================================================================
# GSON
# ============================================================================

-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent Gson from stripping generic types
-keepattributes Signature

# Prevent stripping of @SerializedName annotations
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ============================================================================
# VIEWMODELS & REPOSITORIES
# ============================================================================

# ViewModels - necesarios para ViewModelProvider
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class com.cocido.ramfapp.viewmodels.** { *; }

# ViewModelFactory
-keep class * extends androidx.lifecycle.ViewModelProvider.Factory {
    <init>(...);
}

# Repositories
-keep class com.cocido.ramfapp.repository.** { *; }

# ============================================================================
# VIEW BINDING & DATA BINDING
# ============================================================================

-keep class * extends androidx.viewbinding.ViewBinding {
    <init>(...);
    public static ** bind(***);
    public static ** inflate(***);
}

# Mantener todas las clases de ViewBinding generadas
-keep class com.cocido.ramfapp.databinding.** { *; }
-keepclassmembers class com.cocido.ramfapp.databinding.** { *; }

# ============================================================================
# UTILS & HELPERS
# ============================================================================

# Managers y Helpers importantes
-keep class com.cocido.ramfapp.utils.AuthManager { *; }
-keep class com.cocido.ramfapp.utils.AuthHelper { *; }
-keep class com.cocido.ramfapp.utils.** { *; }

# ============================================================================
# ACTIVITIES, FRAGMENTS & UI
# ============================================================================

# Activities
-keep public class * extends android.app.Activity
-keep public class * extends androidx.appcompat.app.AppCompatActivity
-keep class com.cocido.ramfapp.ui.activities.** { *; }

# Fragments
-keep public class * extends androidx.fragment.app.Fragment
-keep class com.cocido.ramfapp.ui.fragments.** { *; }

# Adapters
-keep class com.cocido.ramfapp.ui.adapters.** { *; }

# Custom Views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ============================================================================
# ANDROID COMPONENTS
# ============================================================================

-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Parcelable
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
    public *;
}

# ============================================================================
# KOTLIN
# ============================================================================

# Kotlin Metadata
-keep class kotlin.Metadata { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Kotlin Parcelize
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ============================================================================
# ANDROIDX & MATERIAL DESIGN
# ============================================================================

# AndroidX
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# Material Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Navigation Component
-keep class androidx.navigation.** { *; }

# Lifecycle
-keep class androidx.lifecycle.** { *; }

# ============================================================================
# SEGURIDAD
# ============================================================================

# EncryptedSharedPreferences
-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }

# ============================================================================
# GOOGLE SERVICES
# ============================================================================

# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Google Maps
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }

# Google Sign-In
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }

# Firebase (si se usa para push notifications en futuro)
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ============================================================================
# LIBRERÍAS DE TERCEROS
# ============================================================================

# MPAndroidChart (Gráficos)
-keep class com.github.mikephil.charting.** { *; }
-keep interface com.github.mikephil.charting.** { *; }

# Glide (Imágenes)
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
    *** rewind();
}

# Lottie Animations
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# ============================================================================
# LOGGING - REMOVER EN RELEASE
# ============================================================================

# Remover logs de debug en release (mantener solo Error y Warning)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Remover SecurityLogger debug logs en release
-assumenosideeffects class com.cocido.ramfapp.utils.SecurityLogger {
    public *** logUserSecurityEvent(...);
    public *** logAuthenticationEvent(...);
    public *** logDataAccess(...);
    public *** logNetworkSecurityEvent(...);
}

# ============================================================================
# WARNINGS A IGNORAR (Librerías opcionales)
# ============================================================================

-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn sun.misc.**
-dontwarn javax.naming.**

# ============================================================================
# DEBUGGING (Mantener información para stack traces útiles)
# ============================================================================

# Mantener números de línea para stack traces
-keepattributes SourceFile,LineNumberTable

# Renombrar source file attribute para ofuscar nombre original
-renamesourcefileattribute SourceFile

# ============================================================================
# FIN DE CONFIGURACIÓN
# ============================================================================
