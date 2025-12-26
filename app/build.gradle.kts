plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.mnn.cameranote"
    compileSdk = 36
    kotlin {
        jvmToolchain(21)
    }
    defaultConfig {
        applicationId = "com.mnn.cameranote"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
}

dependencies {
// --- 1. Compose 核心 (通过 BOM 管理版本) ---
    val composeBom = platform("androidx.compose:compose-bom:2025.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // 核心 UI 库
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.activity.compose)

    // --- 2. CameraX (相机功能) ---
    implementation(libs.bundles.camera)
    // --- 3. 图像加载 (Coil 3.x) ---
    implementation(libs.coil.compose)
    implementation("me.saket.telephoto:zoomable-image-coil3:0.18.0")
    // --- 4. 依赖注入 (Koin 4.x) ---
    implementation(libs.bundles.koin)

    // --- 5. 数据库 (Room 2.8+) ---
    implementation(libs.bundles.room)
    // 注意：Room 编译插件需在 plugins 中配置 KSP 才能工作
    ksp(libs.room.compiler)
    // --- 6. 导航 (Navigation) ---
    implementation(libs.bundles.nav)

    // --- 7. 权限与工具 ---
    implementation(libs.permissions)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.concurrent.futures.ktx)

    // --- 8. 测试 ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}