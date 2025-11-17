plugins {
    kotlin("jvm")
}

group = "xyz.yhsj.music_impl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    compileOnly("org.pf4j:pf4j:3.13.0")
    compileOnly(kotlin("stdlib")) // 不打包 kotlin-stdlib
    compileOnly("com.github.kevinsawicki:http-request:+")
    compileOnly("com.google.code.gson:gson:2.11.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(18)
}