apply {
    from("build-publish.gradle")
}

plugins {
    id("com.android.library")
    id("kotlin-android")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72")
}

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.3")

    defaultConfig {
        minSdkVersion(1)
        targetSdkVersion(29)
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
    include("*")
    include("*/*")
    archiveFileName.set("uitor_webapp.zip")
    destinationDirectory.set(file("${buildDir}/res/raw"))

    android {
        sourceSets["main"].resources.apply {
            srcDirs(srcDirs + file("${buildDir}/res"))
        }
    }
    dependsOn(":app-web:uglifyjsReleaseArtifact")
}

tasks["preBuild"].dependsOn(assembleRawArtifact)