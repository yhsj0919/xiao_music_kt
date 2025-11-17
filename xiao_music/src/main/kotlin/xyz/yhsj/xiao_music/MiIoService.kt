package xyz.yhsj.xiao_music

import xyz.yhsj.cyf.toModel
import java.security.MessageDigest
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

class MiIOService(private val account: MiAccount, region: String? = null) {

    private val server = "https://" + (if (region.isNullOrBlank() || region == "cn") "" else "$region.") + "api.io.mi.com/app"

    // ---------------- miio_request ----------------
    fun miioRequest(uri: String, data: Map<String, Any>): Map<String, Any?> {
        val headers = hashMapOf<String, String?>(
            "User-Agent" to "iOS-14.4-6.0.103-iPhone12,3--FAKE-UUID",
            "x-xiaomi-protocal-flag-cli" to "PROTOCAL-HTTP2"
        )

        return account.miRequest("xiaomiio", server + uri, { token: MutableMap<String?, Any?>, cookies: MutableMap<String, String?> ->
            cookies["PassportDeviceId"] = token["deviceId"].toString()
            signData(uri, data, (token["xiaomiio"] as List<*>)[0].toString())
        }, headers)
    }

    // ---------------- home_request ----------------
    fun homeRequest(did: String, method: String, params: Any): List<Any> {
        val payload = mapOf(
            "id" to 1,
            "method" to method,
            "accessKey" to "IOS00026747c5acafc2",
            "params" to params
        )

        @Suppress("UNCHECKED_CAST")
        val result = miioRequest("/home/rpc/$did", payload)["result"] as? List<Any>
        return result ?: emptyList()
    }

    fun homeGetProps(did: String, props: List<String>): List<Any> = homeRequest(did, "get_prop", props)

    fun homeGetProp(did: String, prop: String): Any? = homeGetProps(did, listOf(prop)).firstOrNull()

    fun homeSetProp(did: String, prop: String, value: Any): Any {
        val result = homeRequest(did, "set_$prop", if (value is List<*>) value else listOf(value))
        return if (result.firstOrNull() == "ok") 0 else result.firstOrNull() ?: -1
    }

    fun homeSetProps(did: String, props: List<Pair<String, Any>>): List<Any> =
        props.map { homeSetProp(did, it.first, it.second) }

    // ---------------- miot_request ----------------
    fun miotRequest(cmd: String, params: Any): List<Map<String, Any>> {
        @Suppress("UNCHECKED_CAST")
        return miioRequest("/miotspec/$cmd", mapOf("params" to params))["result"] as? List<Map<String, Any>> ?: emptyList()
    }

    fun miotGetProps(did: String, iids: List<Pair<Int, Int>>): List<Any?> {
        val params = iids.map { mapOf("did" to did, "siid" to it.first, "piid" to it.second) }
        return miotRequest("prop/get", params).map { if ((it["code"] as? Int) == 0) it["value"] else null }
    }

    fun miotGetProp(did: String, iid: Pair<Int, Int>): Any? = miotGetProps(did, listOf(iid)).firstOrNull()

    fun miotSetProps(did: String, props: List<Triple<Int, Int, Any>>): List<Int> {
        val params = props.map { mapOf("did" to did, "siid" to it.first, "piid" to it.second, "value" to it.third) }
        return miotRequest("prop/set", params).map { (it["code"] as? Int) ?: -1 }
    }

    fun miotSetProp(did: String, iid: Triple<Int, Int, Any>): Int = miotSetProps(did, listOf(iid)).firstOrNull() ?: -1

    // service.miotAction("", 5 to 1, listOf("测试文本"))
    fun miotAction(did: String, iid: Pair<Int, Int>, args: List<Any> = emptyList()): Int {
        val params = mapOf("did" to did, "siid" to iid.first, "aiid" to iid.second, "in" to args)
        return (miotRequest("action", params).firstOrNull()?.get("code") as? Int) ?: -1
    }

    // ---------------- device_list ----------------
    fun deviceList(name: String? = null, getVirtualModel: Boolean = false, getHuamiDevices: Int = 0): List<Map<String, Any?>> {
        val payload = mapOf("getVirtualModel" to getVirtualModel, "getHuamiDevices" to getHuamiDevices)

        @Suppress("UNCHECKED_CAST")
        val result = miioRequest("/home/device_list", payload)["result"] as? Map<String, Any>
        val list = result?.get("list")?.toString()?.toModel<List<Map<String, Any?>>>()?: emptyList()

        return if (name == "full") {
            list
        } else {
            list.filter { name == null || it["did"].toString().contains(name) }
                .filter { it["model"].toString().contains("wifispeaker") }
                .map {
                    mapOf(
                        "name" to it["name"],
                        "model" to it["model"],
                        "did" to it["did"],
                        "token" to it["token"]
                    )
                }
        }
    }

    // ---------------- 静态方法 ----------------
    companion object {
        fun signNonce(ssecurity: String, nonce: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            md.update(Base64.getDecoder().decode(ssecurity))
            md.update(Base64.getDecoder().decode(nonce))
            return Base64.getEncoder().encodeToString(md.digest())
        }

        fun signData(uri: String, data: Any, ssecurity: String): Map<String, String> {
            val json = toJson(data)
            val nonceBytes = ByteArray(8) + (System.currentTimeMillis() / 60000).toInt().toByteArray()
            val nonce = Base64.getEncoder().encodeToString(nonceBytes)
            val snonce = signNonce(ssecurity, nonce)
            val msg = "$uri&$snonce&$nonce&data=$json"

            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(Base64.getDecoder().decode(snonce), "HmacSHA256"))
            val sign = Base64.getEncoder().encodeToString(mac.doFinal(msg.toByteArray()))

            return mapOf("_nonce" to nonce, "data" to json, "signature" to sign)
        }

        fun toJson(value: Any?): String = when (value) {
            null -> "null"
            is String -> "\"" + value.replace("\"", "\\\"") + "\""
            is Number, is Boolean -> value.toString()
            is Map<*, *> -> value.entries.joinToString(",", "{", "}") {
                toJson(it.key.toString()) + ":" + toJson(it.value)
            }

            is Iterable<*> -> value.joinToString(",", "[", "]") { toJson(it) }
            else -> "\"" + value.toString().replace("\"", "\\\"") + "\""
        }

        private fun Int.toByteArray(): ByteArray = byteArrayOf(
            ((this shr 24) and 0xFF).toByte(),
            ((this shr 16) and 0xFF).toByte(),
            ((this shr 8) and 0xFF).toByte(),
            (this and 0xFF).toByte()
        )
    }
}
