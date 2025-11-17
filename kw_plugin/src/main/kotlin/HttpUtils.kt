package xyz.yhsj.cyf

import com.github.kevinsawicki.http.HttpRequest

object HttpUtils2 {
    /**
     * GET 请求
     *
     * @param url     请求地址
     * @param headers 自定义请求头
     * @param cookies 自定义 Cookie
     * @return 响应内容
     * @throws Exception
     */
    fun get(url: String,params: Map<String, Any>? = null, headers: Map<String, String?>? = null, cookies: Map<String, String?>? = null): HttpRequest {
//        println("GET $url")
        val fullUrl = if (params != null && params.isNotEmpty()) {
            val query = params.entries.joinToString("&") { "${it.key}=${it.value}" }
            "$url?$query"
        } else url
        val request = HttpRequest.get(fullUrl)
            .connectTimeout(15000)
            .readTimeout(15000)
            .headers(headers ?: mutableMapOf())
            .cookies(cookies)


        return request
    }

    /**
     * POST 请求
     *
     * @param url     请求地址
     * @param headers 自定义请求头
     * @param cookies 自定义 Cookie
     * @param data    POST 表单数据
     * @return 响应内容
     * @throws Exception
     */
    fun post(
        url: String,
        headers: Map<String, String?>? = null,
        cookies: Map<String, String?>? = null,
        data: Map<String, Any?>? = null
    ): HttpRequest {
//        println("POST $url")
        val request = HttpRequest.post(url)
            .connectTimeout(15000)
            .readTimeout(15000)
            .headers(headers)
            .cookies(cookies)

        if (data != null) {
            request.form(data)
        }

        return request
    }

}

fun HttpRequest.cookies(cookies: Map<String, String?>?): HttpRequest {
    if (cookies != null && !cookies.isEmpty()) {
        val cookieHeader = cookies.entries.joinToString("; ") { (k, v) -> "$k=$v" }
        this.header("Cookie", cookieHeader)
    }
    return this
}
