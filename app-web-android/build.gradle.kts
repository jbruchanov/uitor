//Single resource file AAR only
//It's for easier distribution of the webclient,
//instead of having it as "binary" in the android project

plugins {
    id("com.android.library")
    id("maven-publish")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        minSdkVersion(1)
        targetSdkVersion(29)
    }
}

project.afterEvaluate {
    publishing {
        publications {
            register("maven", MavenPublication::class) {
                groupId = "com.scurab"
                artifactId = "uitor-web"
                version = rootProject.version.toString()
                artifact(project.tasks.getByName("bundleReleaseAar"))

                pom {
                    name.set("UITor")
                    description.set("UI debug tool for Android")
                    url.set("http://uitor.scurab.com")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            name.set("Jiri Bruchanov")
                            email.set("jbruchanov@gmail.com")
                            organization.set("Bruchanov")
                            organizationUrl.set("http://www.bruchanov.name")
                        }
                    }
                    scm {
                        connection.set("scm:git:https://github.com/jbruchanov/uitor")
                        developerConnection.set("scm:git:https://github.com/jbruchanov/uitor")
                        url.set("https://github.com/jbruchanov/uitor")
                    }
                }
            }
        }
    }
}

val buildAppWebTask = task("buildAppWeb") {
    group = "build"
    doLast {
        copy {
            from(file("${rootProject.childProjects.getValue("app-web").buildDir}/artifact/uitor_webapp.zip"))
            into(file("${project.buildDir}/res/raw"))
        }
    }
    android.sourceSets.getByName("main") {
        res.srcDirs(file("${project.buildDir}/res"))
    }
    dependsOn(":app-web:assembleReleaseZipArtifact")
}

tasks.getByName("preBuild").dependsOn(buildAppWebTask)