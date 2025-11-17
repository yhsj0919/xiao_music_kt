package xyz.yhsj.server.ext

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.io.readByteArray
import xyz.yhsj.server.ext.logger
import xyz.yhsj.server.validator.ValidationUtils
import xyz.yhsj.server.auth.AppSession
import xyz.yhsj.server.entity.resp.CommonResp
import xyz.yhsj.server.imageRootPath
import java.io.File
import java.io.InputStream
import java.net.URLEncoder
import java.util.*
import kotlin.reflect.full.isSubclassOf

/**
 * 成功扩展
 */
suspend fun ApplicationCall.success(block: suspend () -> Any?) {
    val data = block()
    if (data is Unit || data == null) {
//        this.respond(HttpStatusCode.OK, CommonResp.empty())
    } else {
        this.respond(HttpStatusCode.OK, data)
    }
}


/**
 * 数据校验
 */
suspend fun <T : Any> T.validated(vararg groups: Class<*>, block: suspend (data: T) -> Any): Any {
    val result = ValidationUtils.validateEntity(this, *groups)
    return if (result.hasErrors) {
        CommonResp.error(msg = result.errorMsg?.values?.first() ?: "未知参数错误")
    } else {
        block(this)
    }
}

/**
 * 数据校验
 */
suspend fun <T : Any> List<T>.validated(vararg groups: Class<*>, block: suspend (data: List<T>) -> Any): Any {
    val result = ValidationUtils.validateList(this, *groups)
    return if (result.hasErrors) {
        CommonResp.error(msg = result.errorMsg?.values?.first() ?: "未知参数错误")
    } else {
        block(this)
    }
}


/**
 * get扩展
 */
@KtorDsl
@JvmName("getExt")
inline fun Route.getExt(
    path: String,

    crossinline body: suspend RoutingContext.(params: Map<String, String?>, session: AppSession?) -> Any,
): Route {
    return route(path, HttpMethod.Get) {
        handle {
            val params = call.parameters.toMap().map { it.key to it.value.first() }.toMap()
            call.success {
                //这里session不为空，前面有校验，需要session的校验通过才会来到这里，不需要session的不关注这个属性
                body(params, call.principal<AppSession>())
            }
        }
    }
}


/**
 * get扩展
 * @validate 校验的数组，不传则不校验
 */
@KtorDsl
@JvmName("getExt")
inline fun <reified R : Any> Route.getExt(
    path: String,
    vararg validate: Class<*>,
    crossinline body: suspend RoutingContext.(R, session: AppSession?) -> Any,
): Route {
    return route(path, HttpMethod.Get) {
        handle {

            val params = call.parameters.toMap().map { it.key to it.value.first() }.toMap()
            val data: R = if (params.containsKey("params")) {
                fromJson(params["params"].toString())
            } else {
                fromJson(params.json())
            }

            call.success {
                data.validated(*validate) {
                    //这里session不为空，前面有校验，需要session的校验通过才会来到这里，不需要session的不关注这个属性
                    body(data, call.principal<AppSession>())
                }
            }
        }
    }
}


/**
 * post扩展
 * @validate 校验的数组，不传则不校验
 */
@KtorDsl
@JvmName("postFormTyped")
inline fun <reified R : Any> Route.postFormExt(
    path: String,
    vararg validate: Class<*>,
    crossinline body: suspend RoutingContext.(R, session: AppSession?) -> Any,
): Route {
    assert(validate.isNotEmpty()) { "校验不可为空！" }

    return route(path, HttpMethod.Post) {
        handle {
            //这里初始化一个空参数，
            val params = call.receiveParameters().toMap().map { it.key to it.value.firstOrNull() }.toMap()
            val data: R = if (R::class.isSubclassOf(Map::class)) {
                params as R
            } else {
                fromJson(params.json())
            }
            call.success {
//                data.validated(*validate) {
                //这里session不为空，前面有校验，需要session的校验通过才会来到这里，不需要session的不关注这个属性
                body(data, call.principal<AppSession>())
//                }
            }
        }
    }
}


/**
 * post扩展
 * @validate 校验的数组，不传则不校验
 */
@KtorDsl
@JvmName("postTyped")
inline fun <reified R : Any> Route.postExt(
    path: String,
    vararg validate: Class<*>,
    crossinline body: suspend RoutingContext.(R, session: AppSession?) -> Any,
): Route {
    assert(validate.isNotEmpty()) { "校验不可为空！" }

    return route(path, HttpMethod.Post) {
        handle {
            //这里初始化一个空参数，
            val data: R = call.receiveJsonOrNull() ?: new()
            call.success {
                data.validated(*validate) {
                    //这里session不为空，前面有校验，需要session的校验通过才会来到这里，不需要session的不关注这个属性
                    body(data, call.principal<AppSession>())
                }
            }
        }
    }
}

/**
 * post空参数扩展
 */
@KtorDsl
@JvmName("postTyped")
inline fun Route.postExt(
    path: String,
    crossinline body: suspend RoutingContext.(session: AppSession?) -> Any,
): Route {
    return route(path, HttpMethod.Post) {
        handle {
            call.success {
                //这里session不为空，前面有校验，需要session的校验通过才会来到这里，不需要session的不关注这个属性
                body(call.principal<AppSession>())
            }
        }
    }
}


/**
 * post扩展
 * @validate 校验的数组，不传则不校验
 * files: Map<String, Any?>  包含PartData.FileItem  和 List<PartData.FileItem>
 */
@KtorDsl
@JvmName("postTyped")
inline fun <reified R : Any> Route.postFileExt(
    path: String,
    vararg validate: Class<*>,
    crossinline body: suspend RoutingContext.(R, files: ArrayList<PartFile>, session: AppSession?) -> Any,
): Route {
    return route(path, HttpMethod.Post) {
        handle {
            //这里初始化一个空参数，
            val part = call.receiveMultiParts()
            val params = part.items
            val files = part.files

            val data: R = fromJson(params.json())
            call.success {
                data.validated(*validate) {
                    //这里session不为空，前面有校验，需要session的校验通过才会来到这里，不需要session的不关注这个属性
                    body(data, files, call.principal<AppSession>())
                }
            }
        }
    }
}


/**
 * postFile空参数扩展
 */
@KtorDsl
@JvmName("postTyped")
inline fun Route.postFileExt(
    path: String,
    crossinline body: suspend RoutingContext.(params: Map<String, Any?>, files: ArrayList<PartFile>, session: AppSession?) -> Any,
): Route {
    return route(path, HttpMethod.Post) {
        handle {
            val part = call.receiveMultiParts()

            val params = part.items
            val files = part.files

            call.success {
                //这里session不为空，前面有校验，需要session的校验通过才会来到这里，不需要session的不关注这个属性
                body(params, files, call.principal<AppSession>())
            }
        }
    }
}


/**
 * 获取表单参数，支持表单数组的话，请使用[]结尾的Key
 */
suspend fun ApplicationCall.receiveMultiParts(): MyPartData = try {

    val multipartData = receiveMultipart()

    val items = hashMapOf<String, Any?>()
    val files = arrayListOf<PartFile>()
    multipartData.forEachPart { part ->
        when (part) {
            is PartData.FormItem -> {
                val name = part.name ?: ""
                if (name.endsWith("[]")) {
                    items[name.removeSuffix("[]")] =
                        (part.value as List<PartData.FormItem?>).map { item -> item?.value }.toList()
                } else {
                    //这里处理对象传入name类似obj[key]的结构，用到其他类型的再加
                    if (name.contains("[") && name.contains("]")) {
                        val obj = name.split("[")[0]
                        val key = name.split("[")[1].replace("]", "")
                        val map = (items[obj] ?: HashMap<String, Any>()) as HashMap<String, Any>
                        map[key] = part.value
                        items[obj] = map
                    } else {
                        items[name] = part.value
                    }
                }
            }

            is PartData.FileItem -> {
                //TODO 解析列表文件
                val file = PartFile(part.originalFileName as String, part.provider().readRemaining().readByteArray())
                files.add(file)
            }

            else -> {}
        }
        part.dispose()
    }
    MyPartData(items, files)

} catch (th: Throwable) {
    th.printStackTrace()
    MyPartData(hashMapOf(), arrayListOf())
}

data class PartFile(
    val originalFileName: String,
    val byteArray: ByteArray,
)

data class MyPartData(
    val items: HashMap<String, Any?>,
    val files: ArrayList<PartFile>,
)


/**
 * 转换成文件
 * 表单提取文件参数
 */
fun Map<String, Any?>.convertFile(): Map<String, Any?> {
    val file = this.filter { it.value is PartData.FileItem }.map { it.key to it.value as PartData.FileItem? }.toMap()

    val files = this.filter { it.value is List<*> }.filter {
        (it.value as List<*>).filterIsInstance<PartData.FileItem>().isNotEmpty()
    }

    val params = hashMapOf<String, Any?>()
    params.putAll(file)
    params.putAll(files)
    return params
}

/**
 * 转换成参数
 * 表单提取文本参数
 */
//fun Map<String, Any?>.convertItem(): Map<String, Any?> {
//    val form =
//        this.filter { it.value is PartData.FormItem }.map { it.key to (it.value as PartData.FormItem?)?.value }.toMap()
//
//    val forms = this.filter { it.value is List<*> }.filter {
//        (it.value as List<*>).filterIsInstance<PartData.FormItem>().isNotEmpty()
//    }.map { it.key to (it.value as List<PartData.FormItem?>).map { item -> item?.value }.toList() }.toMap()
//
//    val params = hashMapOf<String, Any?>()
//    params.putAll(form)
//    params.putAll(forms)
//    return params
//}

/**
 * 保存图片
 * 请保证map里只有PartData.FileItem和List<PartData.FileItem>
 *
 */
fun ArrayList<PartFile>.saveFile(root: String = imageRootPath, folder: String, name: String? = null): List<String> {
    //存图片
    val rootFolder = File("$root/${folder}")
    if (!rootFolder.exists()) {
        rootFolder.mkdirs()
    }

    val images = ArrayList<String>()
    var lastTimestamp = -1L
    this.forEach { file ->
        var currentTimestamp = Date().time

        if (currentTimestamp == lastTimestamp) {
            currentTimestamp++  // 如果时间戳相同，则手动调整
        }

        lastTimestamp = currentTimestamp
        val fileName = name ?: "image_${currentTimestamp}.jpg"
        val fileBytes = file.byteArray
        images.add("$folder/$fileName")

        File("${rootFolder.path}/$fileName").writeBytes(fileBytes)
        Thread.sleep(10); // 暂停 10 毫秒
    }

    return images
}

/**
 * 获取图片
 */
fun Map<String, Any?>.getFile(name: String): PartData.FileItem? {
    this.forEach { (tmpName, file) ->
        if (file != null) {
            if (file is PartData.FileItem) {
                if (tmpName == name) {
                    return file
                }
            }
        }
    }
    return null
}

/**
 * 文件下载名
 */
fun String.downloadName(): String {
    val fileName = URLEncoder.encode(this, "UTF-8")
    return "attachment;filename=$fileName"
}

/**
 * 获取资源文件
 */
fun RoutingContext.resources(dir: String, name: String): OutgoingContent? {
    return call.resolveResource(name, dir)
}

/**
 * 获取资源文件
 */
@OptIn(InternalAPI::class)
fun Application.resources(
    dir: String? = null,
    name: String,
): OutgoingContent? {
    if (name.endsWith("/") || name.endsWith("\\")) {
        return null
    }
    val normalizedPath = normalisedPath(dir, name)
    for (url in environment.classLoader.getResources(normalizedPath).asSequence()) {
        resourceClasspathResource(
            url,
            normalizedPath
        ) { ContentType.defaultForFileExtension(it.path.extension()) }?.let { content ->
            return content
        }
    }

    return null
}

/**
 * 格式化路径
 */
private fun String.extension(): String {
    val indexOfName = lastIndexOf('/').takeIf { it != -1 } ?: lastIndexOf('\\').takeIf { it != -1 } ?: 0
    val indexOfDot = indexOf('.', indexOfName)
    return if (indexOfDot >= 0) substring(indexOfDot) else ""
}

/**
 * 规范化路径
 */
private fun normalisedPath(resourcePackage: String?, path: String): String {
    val pathComponents = path.split('/', '\\')
    if (pathComponents.contains("..")) {
        throw BadRequestException("Relative path should not contain path traversing characters: $path")
    }
    return (resourcePackage.orEmpty().split('.', '/', '\\') + pathComponents)
        .normalizePathComponents()
        .joinToString("/")
}

/**
 * 根据OutgoingContent获取输入流
 */
fun OutgoingContent.inputStream(): InputStream? {
    return when (this) {
        is LocalFileContent -> {
            readFrom().toInputStream()
        }

        is JarFileContent -> {
            readFrom().toInputStream()
        }

        is URIFileContent -> {
            readFrom().toInputStream()
        }

        else -> {
            null
        }
    }
}


/**
 * Json转换
 */
suspend inline fun <reified T : Any> ApplicationCall.receiveJsonOrNull(): T? {
    return try {
//根据不同类型返回
//if (T::class.isSubclassOf(Map::class)) {
//}else{
        receive(T::class)
//}
    } catch (cause: Throwable) {
        cause.printStackTrace()
        logger.error("参数转换失败，返回空对象")
        null
    }
}