package xyz.yhsj.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import xyz.yhsj.server.api.configureApi
import xyz.yhsj.server.ext.KeyValueStore
import xyz.yhsj.server.ext.NetUtils
import xyz.yhsj.server.plugins.*

fun main(args: Array<String>) {

    val port = args.firstOrNull { it.startsWith("-port=") }?.replace("-port=", "")?.toInt()

    embeddedServer(Netty, port = port ?: 8080, module = Application::module).start(wait = true)
}


fun Application.module() {
    configureKoin()
    configureSerialization()
    configureHTTP()
    configureSecurity()
    configureIntercept()
//        configureAdministration()
    configureMonitoring()
    configureRouting()
    configureApi()
    init()
}
