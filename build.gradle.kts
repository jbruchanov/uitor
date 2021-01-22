import java.net.URI

group = "com.scurab"
version = "2.0.0"

buildscript {
    repositories {
        mavenLocal()
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
    }
}

subprojects.forEach {
    it.repositories {
        mavenLocal()
        mavenCentral()
        google()
        jcenter()
        maven {
            url = URI.create("https://dl.bintray.com/kotlin/kotlin-eap")
        }
    }

    it.buildscript {
        repositories {
            mavenLocal()
            mavenCentral()
            jcenter()
        }
    }
}

apply(plugin = "maven-publish")