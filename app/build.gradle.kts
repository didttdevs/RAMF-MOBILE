import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.services)
    id("kotlin-parcelize")
}

val localProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { load(it) }
    }
}

val mapsApiKey: String = System.getenv("MAPS_API_KEY")
    ?: localProperties.getProperty("MAPS_API_KEY")
    ?: ""

android {
    namespace = "com.cocido.ramfapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cocido.ramfapp"
        minSdk = 24  // Bajado a 24 para mayor compatibilidad (Android 7.0+)
        targetSdk = 34
        versionCode = 2  // Incrementado para nueva versión
        versionName = "1.5.0"  // Versión actual del análisis

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Vector drawables para compatibilidad
        vectorDrawables.useSupportLibrary = true
        
        // Recursos configurables
        resourceConfigurations.addAll(listOf("es", "en"))  // Solo español e inglés

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    // Configuración de firmado (signing)
    signingConfigs {
        create("release") {
            // IMPORTANTE: En producción, estas claves deben estar en gradle.properties
            // y el archivo debe estar en .gitignore
            // Para desarrollo, usa valores de entorno o archivos locales
            storeFile = file(System.getenv("RAMF_KEYSTORE_PATH") ?: "release-keystore.jks")
            storePassword = System.getenv("RAMF_KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("RAMF_KEY_ALIAS") ?: "ramf-app"
            keyPassword = System.getenv("RAMF_KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            
            buildConfigField("boolean", "DEBUG_MODE", "true")
            buildConfigField("String", "API_BASE_URL", "\"https://ramf.formosa.gob.ar/api/http/\"")
            
            // Permitir backups en debug
            manifestPlaceholders["allowBackup"] = true
        }
        
        release {
            isMinifyEnabled = true
            isDebuggable = false
            isShrinkResources = true
            
            buildConfigField("boolean", "DEBUG_MODE", "false")
            buildConfigField("String", "API_BASE_URL", "\"https://ramf.formosa.gob.ar/api/http/\"")
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Usar signing config de release si está configurado
            try {
                signingConfig = signingConfigs.getByName("release")
            } catch (e: Exception) {
                logger.warn("Release signing config not found. Using debug signing.")
                signingConfig = signingConfigs.getByName("debug")
            }
            
            // No permitir backups en release por seguridad
            manifestPlaceholders["allowBackup"] = false
        }
        
        // Build type staging para pruebas pre-producción
        create("staging") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            
            buildConfigField("boolean", "DEBUG_MODE", "true")
            
            // Staging usa el mismo backend que producción
            buildConfigField("String", "API_BASE_URL", "\"https://ramf.formosa.gob.ar/api/http/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    
    // Material Design
    implementation(libs.material)
    
    // Lifecycle & ViewModel
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    
    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    
    // UI Components
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.preference.ktx)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    
    // Async
    implementation(libs.kotlinx.coroutines.android)
    
    // Security
    implementation(libs.androidx.security.crypto)
    
    // Database (comentado hasta que sea necesario)
    // implementation(libs.androidx.room.runtime)
    // implementation(libs.androidx.room.ktx)
    // kapt(libs.androidx.room.compiler)
    
    // Background work
    implementation(libs.androidx.work.runtime.ktx)
    
    // Data storage
    implementation(libs.androidx.datastore.preferences)
    
    // Images
    implementation(libs.glide)
    // kapt(libs.glide.compiler) // Comentado para evitar problemas KAPT
    
    // Charts
    implementation(libs.mpandroidchart)
    
    // Animations
    implementation(libs.lottie)
    
    // Google Services
    implementation(libs.google.signin)
    implementation(libs.google.maps)
    implementation(libs.google.location)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
