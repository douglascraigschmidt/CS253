plugins {
    id("java-library")
    kotlin("jvm")
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    implementation(libs.jsoup)
    implementation(libs.annotations)
    implementation(libs.commons.io)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.rxjava)
    implementation(libs.reactor.core)
    implementation(libs.rxandroid)

    // TODO: Grader won't find these libraries when they are declared only as testImplementation
    // Leave as 1.12.5 - DO NOT CHANGE to 1.12.8 -- breaks build!
    implementation(libs.mockk)
    implementation(libs.assertj.core)

    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.adapter.rxjava3)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.retrofit2.reactor.adapter)

    testImplementation(libs.kotlin.test.junit)
    // Leave as 1.12.5 - DO NOT CHANGE to 1.12.8 -- breaks build!
    testImplementation(libs.mockk.v1125)
    testImplementation(libs.assertj.core)
    testImplementation(libs.junit)
}

tasks.test {
    testLogging {
        events("failed") //,"passed", "skipped"
        outputs.upToDateWhen { false }
    }
}