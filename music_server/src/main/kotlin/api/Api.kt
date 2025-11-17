package xyz.yhsj.server.api

import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.*
import xyz.yhsj.server.api.apis.musicApi

fun Application.configureApi() {

    routing {
        //执行session校验
        authenticate("session-auth") {
        }
        //下面的不校验
        musicApi()

    }
}
