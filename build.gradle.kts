plugins {
    application
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    id("com.gradleup.shadow") version "9.3.1"
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

val ktorVersion = "3.4.0"

dependencies {
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("ch.qos.logback:logback-classic:1.5.28")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-swagger:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-config-yaml:$ktorVersion")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

tasks.test {
    useJUnitPlatform()
}