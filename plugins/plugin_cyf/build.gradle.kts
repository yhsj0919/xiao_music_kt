plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("com.gradleup.shadow") version "9.2.2"
}

group = "xyz.yhsj.cyf"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":music_impl"))  // 主程序接口
    compileOnly(kotlin("stdlib"))

    compileOnly("org.pf4j:pf4j:3.13.0")
    kapt("org.pf4j:pf4j:3.13.0")

    implementation("com.github.kevinsawicki:http-request:+")
    implementation("com.google.code.gson:gson:2.11.0")

}

tasks.test {
    useJUnitPlatform()
}

//处理djl打包丢失文件
tasks.shadowJar {
    //https://gradleup.com/shadow/configuration/merging/#handling-duplicates-strategy
//    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    // 排除 kotlin 和 kotlinx 库
    exclude("kotlin/**")
    exclude("com/github/kevinsawicki/**")
    exclude("com/google/gson/**")
//    exclude("kotlinx/**")

    // 包含其他第三方库（如 Ktor、Apache Commons）
//    dependencies {
//        include(dependency("io.ktor:.*"))
//        include(dependency("org.apache.commons:.*"))
//    }

    archiveClassifier.set("") // 去掉默认的 "all" 后缀
    mergeServiceFiles()       // 合并 META-INF/services
    manifest {
        attributes(
            "Plugin-Class" to "xyz.yhsj.cyf.CYFPlugin",
            "Plugin-Id" to "cyf-plugin",
            "Plugin-Version" to "$version",
            "Plugin-Provider" to "cyf",
            "Plugin-Description" to "默认口令(陈一发)，不支持搜索，勿选"
        )
    }

    // 打包完成后自动复制到 app/plugins
    doLast {
        val destDir = rootProject.file("music_server/plugins")
        destDir.mkdirs()
        copy {
            from(archiveFile)
            into(destDir)
        }
        println("✅ 插件已复制到: ${destDir.absolutePath}")
    }
}


// 设置 Kotlin JVM 版本
kotlin {
    jvmToolchain(18)
}
