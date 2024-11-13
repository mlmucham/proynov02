plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "edu.isil.proynov02"
    compileSdk = 34

    defaultConfig {
        applicationId = "edu.isil.proynov02"
        minSdk = 21
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.google.firebase:firebase-database-ktx")

    implementation("com.google.firebase:firebase-storage-ktx")
    // Agregar Glide
    implementation("com.github.bumptech.glide:glide:4.12.0")
    kapt("com.github.bumptech.glide:compiler:4.12.0")

    implementation("com.google.firebase:firebase-auth:21.0.1")

    implementation("com.google.firebase:firebase-appcheck-playintegrity:17.0.0")

    // Agregar Firebase App Check
    implementation("com.google.firebase:firebase-appcheck:16.0.0")
    implementation("com.google.firebase:firebase-appcheck-safetynet:16.0.0")

}

// Aplicar el plugin de servicios de Google
apply(plugin = "com.google.gms.google-services")