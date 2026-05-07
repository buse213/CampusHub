import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.busenurcam.campushub"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.busenurcam.campushub"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true

        // local.properties dosyasından anahtarları okur
        val properties = gradleLocalProperties(rootDir, providers)
        val mapsKey: String = properties.getProperty("MAPS_API_KEY") ?: ""
        val imgbbKey: String = properties.getProperty("IMGBB_API_KEY") ?: ""

        // Manifest için (Google Maps)
        manifestPlaceholders["MAPS_API_KEY"] = mapsKey

        // Java kodundan BuildConfig.MAPS_API_KEY ve BuildConfig.IMGBB_API_KEY olarak erişmek için:
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsKey\"")
        buildConfigField("String", "IMGBB_API_KEY", "\"$imgbbKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // BuildConfig sınıfının otomatik oluşmasını sağlayan kritik blok
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")

    // Google Maps & Location
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // ImgBB için Network Kütüphaneleri (Kritik!)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Glide (Görsel Gösterme)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Android Standart
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.multidex:multidex:2.0.1")

    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
}