plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    id("kotlin-parcelize")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
    }
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

android {
    namespace = "edu.vanderbilt.crawler"

    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

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

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
            File("${project.projectDir}/src/main/res-screen").listFiles()?.forEach {
                res.srcDirs(it.path)
            }
        }

        getByName("test") {
            java.srcDirs("src/test/kotlin")
        }
    }


    // Androidx test
    useLibrary("android.test.runner")
    useLibrary("android.test.base")
    useLibrary("android.test.mock")

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,LICENSE.md,LICENSE-notice.md}"
        }
    }

    configurations.all {
        resolutionStrategy.force("com.google.code.findbugs:jsr305:3.0.2")
    }

    buildFeatures {
        viewBinding = true
    }

    lint {
        abortOnError = false
    }
}

tasks.withType<Test>().configureEach {
    testLogging {
        events("failed") // ,"passed", "skipped", "failed", "standardOut", "standardError"
        showStandardStreams = true
    }
    outputs.upToDateWhen { false }
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    implementation(project(":image-crawler"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.annotation)
    implementation(libs.appcompat)
    implementation(libs.recyclerview)
    implementation(libs.constraintlayout)

    implementation(libs.lifecycle.extensions)
    implementation(libs.lifecycle.common.java8)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.preference.ktx)
    implementation(libs.core.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.material)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlin.reflect)
    implementation(libs.picasso)
    implementation(libs.okhttp.integration)
    implementation(libs.gson)
    implementation(libs.commons.io)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.rxbinding)
    implementation(libs.stetho)

    debugImplementation(libs.leakcanary.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)

    androidTestImplementation(libs.core)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.rules)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.google.truth)
    androidTestImplementation(libs.jsr305)
    androidTestImplementation(libs.espresso.core)
}
