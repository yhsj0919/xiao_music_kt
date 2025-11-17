package xyz.yhsj.xiao_music

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.yhsj.xiao_music.utils.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import javax.naming.AuthenticationException

class MiAccount(
    var username: String? = null,
    var password: String? = null,
    var tokenPath: String = "./mi_token.json"
) {

    private val tokenStore: MiTokenStore = MiTokenStore(tokenPath)
    private var token: MutableMap<String?, Any?>?
    private val userAgent: String?
    private val LOGGER: Logger = LoggerFactory.getLogger(MiAccount::class.java)
    var isLogin = false

    init {
        this.token = tokenStore?.loadToken()
        this.userAgent = this.randomUA
    }

    private val randomUA: String
        get() {
            val uas = arrayOf(
                "MiHome/6.0.103 (iOS XiaoAiApp)",
                "MiHome/6.0.103 (iOS XiaoAiApp)",
                "MiHome/6.0.103 (iOS XiaoAiApp)",
                "MiHome/6.0.103 (iOS XiaoAiApp)"
            )
            return uas[Random().nextInt(uas.size)]
        }

    fun login(sid: String? = "micoapi"): Boolean {
        if (username == null || password == null) {
            throw NullPointerException("username or password is null")
        }
        if (token == null) {
            token = HashMap<String?, Any?>()
            token!!["deviceId"] = getRandomString(16).uppercase(Locale.getDefault())
        }
        try {
            var resp = serviceLogin("serviceLogin?sid=$sid&_json=true", null)
            if ((resp["code"] as Number).toInt() != 0) {
                println(">>>>>>>>>>>>新的请求>>>>>>>>>>>>>>>>>>>>")
                val data: MutableMap<String, Any?> = LinkedHashMap<String, Any?>()
                data["_json"] = "true"
                data["qs"] = resp["qs"] as String?
                data["sid"] = resp["sid"] as String?
                data["_sign"] = resp["_sign"] as String?
                data["callback"] = resp["callback"] as String?
                data["user"] = username
                data["hash"] = md5(password!!).uppercase(Locale.getDefault())

                resp = serviceLogin("serviceLoginAuth2", data)
                if ((resp["code"] as Number).toInt() != 0) throw RuntimeException(
                    "Login failed: " + gson.toJson(
                        resp
                    )
                )
                println("请求结果2：$resp")
            }

            token!!["userId"] = resp["userId"].toString()
            token!!["passToken"] = resp["passToken"]
            println("请求结果1：$resp")
            val ssecurity = resp["ssecurity"].toString()
            val nonce = resp["nonce"].toString()
            val location = resp["location"].toString()

            if (location != "null" && location != "") {
                val serviceToken = securityTokenService(location, nonce, ssecurity)
                token!![sid] = listOf(ssecurity, serviceToken)

                tokenStore?.saveToken(token)
                isLogin = true
                return true
            } else {
                println("认证链接：${resp["notificationUrl"]}")
                throw AuthenticationException("登录需要认证，请先在局域网登录账号重试")
            }

        } catch (e: Exception) {
            LOGGER.error("Exception on login {}: {}", username, e.message, e)
            token = null
            tokenStore?.deleteToken()
            isLogin = false
            return false
        }
    }

    private fun serviceLogin(uri: String, data: MutableMap<String, Any?>?): Map<String, Any?> {
        val headers: MutableMap<String, String?> = LinkedHashMap<String, String?>()
        headers["User-Agent"] = userAgent

        val cookies: MutableMap<String, String?> = LinkedHashMap<String, String?>()
        cookies["sdkVersion"] = "3.9"
        cookies["deviceId"] = token!!["deviceId"] as String?
        if (token!!.containsKey("passToken")) {
            cookies["userId"] = token!!["userId"] as String?
            cookies["passToken"] = token!!["passToken"] as String?
        } else {
            cookies["passToken"] = ""
        }

        val url = "https://account.xiaomi.com/pass/$uri"

        val response = if (data == null) HttpUtils.get(url, headers, cookies) else HttpUtils.post(url, headers, cookies, data)

        val raw = response.body()
        // JSON starts after "&&&START&&&"
        val jsonPart = raw.substring(11)
        val resp: Map<String, Any?> = jsonPart.toMap()
//        if (resp.containsKey("notificationUrl")) {
//            println("Account verification is required:" + resp["notificationUrl"])
//
//            throw RuntimeException("Account verification is required")
//        } else {
        LOGGER.debug("{}: {}", uri, gson.toJson(resp))
        return resp
//        }
    }

    @Throws(Exception::class)
    private fun securityTokenService(location: String?, nonce: String, ssecurity: String?): String {
        val nsec = "nonce=$nonce&$ssecurity"
        val clientSign = Base64.getEncoder().encodeToString(sha1(nsec))

        val finalUrl = location + "&clientSign=" + URLEncoder.encode(clientSign, StandardCharsets.UTF_8)


        val resp = HttpUtils.get(finalUrl)


        var serviceToken: String? = null
        for (header in resp.headers("Set-Cookie")) {
            if (header.startsWith("serviceToken=")) {
                serviceToken = header.split("=".toRegex(), limit = 2).toTypedArray()[1].split(";".toRegex(), limit = 2)
                    .toTypedArray()[0]
                break
            }
        }
        if (serviceToken == null) {
            throw RuntimeException("serviceToken missing: " + resp.body())
        }
        return serviceToken
    }

    fun miRequest(
        sid: String?,
        url: String,
        data: Any?,
        headers: HashMap<String, String?>?,
        relogin: Boolean = true
    ): Map<String, Any?> {
        if ((token != null && token!!.containsKey(sid)) || login(sid)) {
            val myHeaders = headers ?: HashMap<String, String?>()
            myHeaders["User-Agent"] = userAgent
            val sidInfo = token!![sid] as MutableList<*>
            val serviceToken = sidInfo[1] as String?

            val cookies: MutableMap<String, String?> = LinkedHashMap<String, String?>()
            cookies["userId"] = token!!["userId"] as String?
            cookies["serviceToken"] = serviceToken


            // 处理 Python 里的 callable(data)
            val requestData: Map<String, String>? = when (data) {
                is Function2<*, *, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    (data as (MutableMap<String?, Any?>, MutableMap<String, String?>) -> Map<String, String>)
                        .invoke(token!!, cookies)
                }

                is Map<*, *> -> {
                    data as Map<String, String>?
                }

                else -> null
            }

            val resp = if (requestData == null) HttpUtils.get(url, myHeaders, cookies) else HttpUtils.post(
                url,
                myHeaders,
                cookies,
                requestData
            )

            LOGGER.info("{} {}", if (data == null) "GET" else "POST", url)

            if (resp.code() == 200) {
                val respJson: Map<String, Any?> = resp.body().toMap()
                if (respJson["code"].toString().toInt() == 0) return respJson
                if (respJson["message"].toString().lowercase(Locale.getDefault()).contains("auth")) {
                    if (relogin) {
                        println("Auth error, re-login...")
                        token = null
                        return miRequest(sid, url, data, headers, false)
                    }
                }
                throw Exception("Error: " + respJson.get("message"))
            } else {
                println("Login Error :" + resp.code())
                println(resp.body())
                tokenStore?.deleteToken()
                if (relogin) {
                    miRequest(sid, url, data, headers, false)
                } else {
                    throw RuntimeException("HTTP " + resp.code() + ": " + resp.body())
                }
//
            }
        }
        throw RuntimeException("Login failed for sid " + sid)
    }

    /**
     * 获取最新消息
     */
    fun getLatestAskFromXiaoAi(deviceId: String, hardware: String, sid: String = "micoapi", relogin: Boolean = true): Map<String, Any>? {
        if ((token != null && token!!.containsKey(sid)) || login(sid)) {
            val myHeaders = hashMapOf("User-Agent" to userAgent)

            val sidInfo = token!![sid] as MutableList<*>
            val serviceToken = sidInfo[1] as String?
            val cookies: MutableMap<String, String?> = LinkedHashMap<String, String?>()
            cookies["userId"] = token!!["userId"].toString()
            cookies["serviceToken"] = serviceToken
            cookies["deviceId"] = deviceId

            val url = "https://userprofile.mina.mi.com/device_profile/v2/conversation?source=dialogu&hardware={hardware}&limit=2".replace("{hardware}", hardware);

            try {
                val body = HttpUtils.get(url, myHeaders, cookies).bytes()
                val response = String(body, Charsets.UTF_8)
                val info = response.toMap()
                val data = info["data"].toString().toMap()
                val ss = (data["records"].json().toModel<List<Map<String, Any?>>>()).get(0)
                val query = ss.get("query").toString()
                val time = ss.get("time").toString().toLong()
                return hashMapOf("query" to query, "time" to time)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null


        }
        return null
    }

    fun cleanToken() {
        token = null
        tokenStore.deleteToken()
    }

}
