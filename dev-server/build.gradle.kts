plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.50"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    val ktor_version = "1.2.4"
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-websockets:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}