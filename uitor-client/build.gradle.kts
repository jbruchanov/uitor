apply {
    from("build-publish.gradle")
}

plugins {
    id("com.android.library")
    id("kotlin-android")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.10")
}

android {
    compileSdk = 31

    defaultConfig {
        minSdkVersion(1)
        targetSdkVersion(31)
    }
}

project.afterEvaluate {
    extensions.getByType(PublishingExtension::class).apply {
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
    from(file("${project.rootDir}/app-web/build/distributions"))
    include("*")
    include("*/*")
    exclude("*.map")
    archiveFileName.set("uitor_webapp.zip")
    destinationDirectory.set(file("${buildDir}/res/raw"))

    android {
        sourceSets["main"].res.apply {
            srcDirs(srcDirs + file("${buildDir}/res"))
        }
    }
    dependsOn(":app-web:browserProductionWebpack")
}

tasks["preBuild"].dependsOn(assembleRawArtifact)