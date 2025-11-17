plugins {
    kotlin("jvm")
    id("io.ktor.plugin") version "3.3.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
    kotlin("kapt")
    id("com.gradleup.shadow") version "9.2.2"


}

group = "xyz.yhsj.server"
version = "1.0-SNAPSHOT"
application {
    mainClass.set("xyz.yhsj.server.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {


    //https://github.com/ktorio/ktor
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-host-common")
    //状态码
    implementation("io.ktor:ktor-server-status-pages")
    //跨域
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-server-netty")
    //数据序列化
    implementation("io.ktor:ktor-serialization-jackson")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("com.google.code.gson:gson:2.11.0")

    //日志
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("io.ktor:ktor-server-call-logging")

    //JWT，权限认证
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-sessions")


    //校验
    //https://github.com/hibernate/hibernate-validator
    implementation("org.hibernate.validator:hibernate-validator:8.0.3.Final")
    implementation("org.glassfish:jakarta.el:4.0.2")

    // 依赖注入
    //https://github.com/InsertKoinIO/koin
    implementation("io.insert-koin:koin-core:4.1.1")
    implementation("io.insert-koin:koin-ktor:4.1.1")
    implementation("io.insert-koin:koin-logger-slf4j:4.1.1")

    //xiao_music
    implementation(project(":xiao_music"))

    //插件
    kapt("org.pf4j:pf4j:3.13.0")
    implementation("org.pf4j:pf4j:3.13.0")
    implementation(project(":music_impl"))


    implementation("com.github.kevinsawicki:http-request:+")
    implementation("com.google.code.gson:gson:2.11.0")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(18)
}