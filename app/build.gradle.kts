import org.jetbrains.kotlin.fir.expressions.builder.buildArgumentList

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    kotlin("kapt")
}

android {
    namespace = "com.mihaidornea.opencvapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mihaidornea.opencvapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        multiDexEnabled = true

        externalNativeBuild {
            cmake {
                arguments("-DOpenCV_DIR=C:\\Users\\mdorn\\AndroidStudioProjects\\OpenCVApp\\sdk\\native\\jni",
                    "-DANDROID_TOOLCHAIN=clang",
                    "-DANDROID_STL=c++_static")
                targets("opencvapp")
            }
        }

        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
            ldLibs?.add("log")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation(project(path = ":renderscript-toolkit"))
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // RxPermissions
    implementation(libs.rxPermissions)
    implementation(libs.rxAndroid)

    // Koin
    implementation(libs.koinCore)
    implementation(libs.koinAndroid)

    //Coroutines
    implementation(libs.coroutinesCore)
    implementation(libs.coroutinesAndroid)

    //USB camera
    implementation(libs.androidUSBCamera)
}