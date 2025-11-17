package xyz.yhsj.server.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import xyz.yhsj.server.auth.AppSession
import xyz.yhsj.server.session.sessionAuth


fun Application.configureSecurity() {
    install(Sessions) {
        cookie<AppSession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }
    //session校验
    install(Authentication) {
        sessionAuth()
    }
}
