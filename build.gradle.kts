plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    application
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
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.vlr.scrapper.ServerKt")
}

tasks.test {
    useJUnitPlatform()
}
