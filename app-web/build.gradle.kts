plugins {
    id("org.jetbrains.kotlin.js") version "1.3.50"
}

project.apply {
    plugin("kotlin-dce-js")
}
project.apply("builddev.gradle.kts")

group = "com.scurab"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    //TODO something better
    val kotlinxHtmlJsVersion : String by project
    val kotlinxCoroutinesCoreJsVersion : String by project

    implementation(project(":common"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:$kotlinxHtmlJsVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$kotlinxCoroutinesCoreJsVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-js")
}

kotlin.target.browser { }