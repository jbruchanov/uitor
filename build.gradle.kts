import java.net.URI

group = "com.scurab"
version = "1.0-SNAPSHOT"

subprojects.forEach {
    it.repositories {
        mavenCentral()
        google()
        jcenter()
        maven {
            url = URI.create("https://dl.bintray.com/kotlin/kotlin-eap")
        }
    }

    it.buildscript {
        repositories {
            mavenCentral()
            jcenter()
        }
    }
}

apply(plugin = "maven-publish")