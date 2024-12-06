plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)

}

android {
    namespace = "com.comp3040.mealmate"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.comp3040.mealmate"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes.addAll(
                listOf(
                    "META-INF/LICENSE-notice.md",
                    "META-INF/LICENSE.md",
                    "META-INF/LICENSE.txt",
                    "META-INF/NOTICE.md",
                    "META-INF/NOTICE.txt"
                )
            )
        }
    }
}

dependencies {

    // Core dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.coil.compose)
    implementation(libs.accompanist.pager.indicators)
    implementation(libs.accompanist.pager)
    implementation(libs.androidx.lifecycle.runtime.ktx.v2xx)
    implementation(libs.androidx.runtime.livedata)
    implementation("com.github.bumptech.glide:glide:4.13.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0")
    implementation("androidx.compose.foundation:foundation:1.7.5")
    implementation(libs.firebase.database)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation("androidx.compose.ui:ui-tooling:1.7.5")
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation("androidx.camera:camera-core:1.4.0")
    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")

    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.mlkit:image-labeling:17.0.9")
    implementation(libs.logging.interceptor)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation("com.google.guava:guava:32.1.3-android")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation(libs.firebase.database.v2010)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation(libs.androidx.junit.ktx)

    // Testing dependencies
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.22") // Kotlin test library
    testImplementation(libs.junit) // JUnit 4
    testImplementation("io.mockk:mockk:1.13.5") // MockK for mocking
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.mockito:mockito-inline:4.0.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation(libs.mockito.mockito.core.v4110)
    testImplementation(libs.powermock.api.mockito2)
    testImplementation(libs.powermock.module.junit4)
    testImplementation(libs.junit.jupiter)

    // AndroidX Test libraries
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Compose UI testing
    androidTestImplementation(libs.androidx.ui.test.junit4.v151)
    debugImplementation(libs.androidx.compose.ui.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    // AndroidX Test Rules and Runner
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")


    // Add MockK for Android instrumentation tests
    androidTestImplementation (libs.mockk.android)
    testImplementation (libs.mockk.android )// Use the Android-specific MockK library
    testImplementation (libs.androidx.core) // Required for ActivityScenario
    androidTestImplementation(libs.mockk.android)
    testImplementation (libs.robolectric)
    implementation ("androidx.appcompat:appcompat:1.6.1") // Or the latest version
    androidTestImplementation ("androidx.test.uiautomator:uiautomator:2.2.0")


        testImplementation ("org.mockito:mockito-core:4.8.0")  // Mockito core library
        androidTestImplementation ("org.mockito:mockito-android:5.14.2") // Mockito for Android testing
        testImplementation ("org.mockito.kotlin:mockito-kotlin:4.0.0") // Kotlin extension for Mockito

    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.0")
    testImplementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation(libs.core.testing.v220)

    // LiveData dependencies
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.0")

    // Test dependencies
    testImplementation ("androidx.arch.core:core-testing:2.1.0")
    implementation ("com.google.firebase:firebase-core:9.6.1")
    testImplementation("org.robolectric:robolectric:4.9")

}

