apply {
    from("build-publish.gradle")
}

plugins {
    id("com.android.library")
    id("kotlin-android")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.21")
}

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(1)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
    }
}

project.afterEvaluate {
    extensions.getByType(PublishingExtension::class.java).apply {
        publications {
            getByName<MavenPublication>("maven") {
                artifactId = "anuitor-client"
                artifact(tasks["bundleReleaseAar"])
            }
        }
    }
}

val assembleRawArtifact = task<Zip>("assembleRawArtifact") {
    group = "custombuild"
    from(file("${project.rootDir}/app-web/src/main/resources"))
    from(file("${project.rootDir}/app-web/build/out/uitor.min.js"))
    from(file("${project.rootDir}/app-web/build/out/index.html"))
    include("*")
    include("*/*")
    archiveFileName.set("uitor_webapp.zip")
    destinationDirectory.set(file("${buildDir}/res/raw"))

    android {
        sourceSets["main"].res.apply {
            srcDirs(srcDirs + file("${buildDir}/res"))
        }
    }
    dependsOn(":app-web:createReleaseIndexHtml")
}

tasks["preBuild"].dependsOn(assembleRawArtifact)