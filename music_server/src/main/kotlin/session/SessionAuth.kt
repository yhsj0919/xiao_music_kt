package xyz.yhsj.server.session

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import xyz.yhsj.server.auth.AppSession
import xyz.yhsj.server.entity.resp.CommonResp


/**
 * session校验
 */
fun AuthenticationConfig.sessionAuth(name: String = "session-auth") {
    session<AppSession>(name) {
        skipWhen { call ->
            val skipPath = arrayListOf("/admin/login")
            call.request.path() in skipPath
        }
        validate { session ->
            // 判断 Session 是否有效
            //if (session.userId.isNotBlank()) session else null
            //这里暂时不校验了，以后需要登录再写
            session
        }


        challenge {
            call.sessions.set(AppSession(token = "sssssss"))
            call.respond(HttpStatusCode.OK, CommonResp.login())
        }
    }
}