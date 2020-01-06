import java.net.URI

group = "com.scurab"
version = "2.0.0"

buildscript {
    repositories {
        mavenCentral()
        google()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:3.5.3")
    }
}

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
            google()
            jcenter()
        }
    }
}

apply(plugin = "maven-publish")