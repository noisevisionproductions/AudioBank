import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {

    alias(libs.plugins.kotlinMultiplatform)
    id("com.android.application") version "8.7.1"
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("com.google.gms.google-services")
    id("kotlinx-serialization")
}

repositories {
    google()
    mavenCentral()
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant {
            sourceSetTree.set(KotlinSourceSetTree.test)
            dependencies {
                implementation(libs.core.ktx)
                implementation(libs.compose.ui.test.junit4.android)
                debugImplementation(libs.compose.ui.test.manifest)
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    )

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.coil.compose)
            runtimeOnly(libs.androidx.fragment.ktx)

            // Azure Storage and components for XML

            implementation(libs.woodstox.core)
            implementation(libs.stax.api)
            // Firebase auth
            implementation(libs.firebase.auth.ktx)

            // Firebase Firestore
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.firestore.ktx)

            // Firebase Storage
            implementation(libs.google.firebase.storage.v1102)
            implementation(libs.firebase.appcheck.debug)

        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlinx.serialization.json)
        }

        iosMain.dependencies {

        }
    }
}

android {
    namespace = "org.noisevisionproductions.audiobank"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")


    defaultConfig {
        applicationId = "org.noisevisionproductions.samplelibrary"
        vectorDrawables.useSupportLibrary = true
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        minSdk = 26
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/io.netty.versions.properties"

        }
    }

    testBuildType = "debug"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}
dependencies {
    implementation(libs.androidx.navigation.runtime.ktx)
}

