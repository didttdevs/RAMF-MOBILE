plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.rafapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.rafapp"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    // Habilitar View Binding en Kotlin DSL
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Ingreso con google
    implementation(libs.google.signin)

    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation(libs.androidx.lifecycle.runtime.ktx)

    //Glide para imagenes

    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    //Swipe refresh
    implementation(libs.androidx.swiperefreshlayout)

    // AndroidX y Material Design
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Retrofit para llamadas a la API
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Coroutines para manejo asíncrono
    implementation(libs.kotlinx.coroutines.android)

    // ViewModel y LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Android Charts
    implementation(libs.mpandroidchart)
}
