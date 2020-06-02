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
        classpath("com.android.tools.build:gradle:3.6.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
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
            jcenter()
        }
    }
}

apply(plugin = "maven-publish")