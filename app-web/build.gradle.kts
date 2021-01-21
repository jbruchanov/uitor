plugins {
    kotlin("js")
}

//project.apply("builddev.gradle.kts")

group = "com.scurab"
version = project.ext.get("releaseVersion") ?: throw NullPointerException("Undefined 'releaseVersion'")

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:${Versions.kotlinHtmlJs}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:${Versions.kotlinCoroutines}")
    testImplementation(kotlin("test-js"))
}

kotlin {
    js(LEGACY) {
        browser {
            binaries.executable()
            //webpack config in webpack.config.d/devServer.js
            webpackTask {
                cssSupport.enabled = true
            }
            runTask {
                cssSupport.enabled = true
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
    }
}