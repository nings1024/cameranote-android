plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)
}

// 注意：jvmToolchain 是 Kotlin 扩展，不要写在 android {} 里面（那里只是碰巧能解析到）
kotlin {
    jvmToolchain(21)
}

android {
    namespace = "com.mnn.cameranote"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mnn.cameranote"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            // 开启 R8 与资源压缩：material-icons-extended 这类库没混淆会让包体涨一大截
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --- Compose（BOM 统一版本）---
    implementation(platform(libs.compose.bom))
    androidTestImplementation(platform(libs.compose.bom))

    implementation(libs.compose.core)
    implementation(libs.compose.graphics)
    implementation(libs.compose.toolingPreview)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons)
    implementation(libs.androidx.activity.compose)

    // --- 相机 ---
    implementation(libs.bundles.camera)

    // --- 图片加载 ---
    implementation(libs.coil.compose)
    implementation(libs.telephoto.zoomable)

    // --- 依赖注入 ---
    implementation(libs.bundles.koin)

    // --- 数据库 ---
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // --- 导航 ---
    implementation(libs.bundles.nav)

    // --- 权限与工具 ---
    implementation(libs.permissions)
    implementation(libs.androidx.core.ktx)
    implementation(libs.concurrent.futures.ktx)

    // --- 测试 ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    debugImplementation(libs.compose.tooling)
    debugImplementation(libs.compose.testManifest)
}
