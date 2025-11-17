plugins {
    kotlin("jvm")
}

group = "xyz.yhsj.xiao_music"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
//    compileOnly(kotlin("stdlib"))
    implementation(project(":kw_plugin"))

    implementation("com.github.kevinsawicki:http-request:+")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16") // 简单日志输出，可替换为 logback
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(18)
}