plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.10" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

if (file("$projectDir/admin/skeleton.gradle").isFile) {
    apply(from = "$projectDir/admin/skeleton.gradle")
}