package xyz.yhsj.server.status

import com.google.gson.JsonSyntaxException
import io.ktor.http.*
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.koin.core.error.NoDefinitionFoundException
import xyz.yhsj.server.error.AppException
import xyz.yhsj.server.entity.resp.CommonResp

import java.sql.SQLException

fun StatusPagesConfig.statusPage() {

    status(HttpStatusCode.NotFound) { call, _ ->
        println(call.request.path())
        call.respond(HttpStatusCode.OK, CommonResp.notFound(msg = "路径不存在"))
    }
    status(HttpStatusCode.UnsupportedMediaType) { call, _ ->
        call.respond(HttpStatusCode.OK, CommonResp.error(msg = "不支持的媒体类型"))
    }


    status(HttpStatusCode.InternalServerError) { call, error ->
        call.respond(HttpStatusCode.OK, CommonResp.error(msg = "服务器异常"))
    }
    exception<Throwable> { call, error ->
        error.printStackTrace()
        call.respond(
            HttpStatusCode.OK, when (error) {
                is JsonSyntaxException -> {
                    CommonResp.error(msg = "JSON数据格式错误:${error.cause?.message}")
                }

                is ContentTransformationException -> {
                    CommonResp.error(msg = "JSON数据转换错误")
                }

                is NullPointerException -> {
                    CommonResp.error(msg = "服务器空指针异常")
                }

                is NoDefinitionFoundException -> {
                    CommonResp.error(msg = "依赖注入异常")
                }


                is AppException -> {
                    CommonResp.error(code = error.code, msg = error.message ?: "")
                }

                is SQLException -> {
                    if (error.message?.contains("database exists") == true)
                        CommonResp.error(msg = "数据库已经存在")
                    else if (error.message?.contains("Unknown database") == true)
                        CommonResp.error(msg = "数据库不存在")
                    else if (error.message?.contains("doesn't exist") == true)
                        CommonResp.error(msg = "表不存在")
                    else
                        CommonResp.error(msg = "数据库异常")
                }

                is NotImplementedError -> {
                    CommonResp.error(msg = "接口未实现")
                }

                else -> {
                    error.printStackTrace()
                    CommonResp.error(msg = "服务器异常")
                }
            }
        )
    }
}