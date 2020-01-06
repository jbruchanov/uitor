plugins {
    id("org.jetbrains.kotlin.js") version Versions.kotlin
}

project.apply {
    plugin("kotlin-dce-js")
}
project.apply("builddev.gradle.kts")

group = rootProject.group
version = rootProject.version

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:${Versions.kotlinHtmlJs}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:${Versions.kotlinCoroutines}")
    testImplementation("org.jetbrains.kotlin:kotlin-test-js")
}

kotlin.target.browser { }