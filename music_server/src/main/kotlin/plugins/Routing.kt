package xyz.yhsj.server.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import xyz.yhsj.server.status.statusPage

fun Application.configureRouting() {
    //请求头
//    install(AutoHeadResponse)

    //404,500,异常处理
    install(StatusPages) {
        statusPage()
    }
}
