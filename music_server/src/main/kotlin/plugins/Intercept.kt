package xyz.yhsj.server.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import xyz.yhsj.server.ext.logger

/**
 * 拦截器
 */
fun Application.configureIntercept() {
    //拦截器
    intercept(ApplicationCallPipeline.Call) {
        if (!call.request.path().startsWith("/openapi")) {
//            logger.info("Authorization:" + call.request.header("Authorization"))
//            logger.info("User-Agent:" + call.request.header("User-Agent"))
//        call.request.headers.forEach { s, strings ->
//            logger.info("$s=>${strings}")
//        }
//        println(call.request.cookies.rawCookies)
            logger.info("${call.request.httpMethod.value} http://" + call.request.host() + ":" + call.request.port() + call.request.path())
        }
    }

    //以后配置防止重复点击
    //记录每个session的请求连接时间,连续两次的时间放在redis做个对比,小于某个时间拦截
}

