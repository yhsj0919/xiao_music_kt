package xyz.yhsj.xiao_music

import xyz.yhsj.xiao_music.entity.XiaoMusicDevice
import xyz.yhsj.xiao_music.entity.XiaoMusicResp
import xyz.yhsj.xiao_music.entity.XiaoMusicUrl
import xyz.yhsj.xiao_music.utils.getRandomString
import xyz.yhsj.xiao_music.utils.json
import xyz.yhsj.xiao_music.utils.toMap
import xyz.yhsj.xiao_music.utils.toModel
import java.util.*


private val usePlayMusicApi =
    listOf("LX04", "LX05", "L05B", "L05C", "L06", "L06A", "X08A", "X10A", "X08C", "X08E", "X8F")

class MiNAService(private val account: MiAccount) {

    val defaultAudioId = "1696420661267792487"


    private val device2hardware = mutableMapOf<String, String>()

    /**
     * 发起 Mina API 请求
     */
    fun minaRequest(uri: String, data: MutableMap<String, Any>? = null): Map<String, Any?>? {
        val requestId = "app_ios_${getRandomString(30)}"
        val _uri = if (data != null) {
            data["requestId"] = requestId
            uri
        } else {
            "$uri&requestId=$requestId"
        }

        val headers = hashMapOf<String, String?>(
            "User-Agent" to "MiHome/6.0.103 (com.xiaomi.mihome; build:6.0.103.1; iOS 14.4.0) Alamofire/6.0.103 MICO/iOSApp/appStore/6.0.103"
        )

        return account.miRequest("micoapi", "https://api2.mina.mi.com$_uri", data = data, headers = headers)
    }

    /**
     * 获取设备列表
     */
    fun deviceList(master: Int = 0): List<XiaoMusicDevice> {
        val result = minaRequest("/admin/v2/device_list?master=$master")
        val data = result.json().toModel<XiaoMusicResp<List<XiaoMusicDevice>>>()
        return data.data ?: arrayListOf()
    }

    /**
     * Ubus 请求
     */
    fun ubusRequest(
        deviceId: String,
        method: String,
        path: String,
        message: Map<String, Any>
    ): Map<String, Any?>? {
        val msgStr = message.json()
        return minaRequest(
            "/remote/ubus", mutableMapOf(
                "deviceId" to deviceId,
                "method" to method,
                "path" to path,
                "message" to msgStr
            )
        )
    }

    /**
     * 获取最新问答
     */
    fun getLatestAskFromXiaoAi(deviceId: String): Map<String, Any>? {
        if (!device2hardware.containsKey(deviceId)) {
            initDevices()
        }
//        println(device2hardware)
        val hardware = device2hardware[deviceId]!!
        return account.getLatestAskFromXiaoAi(deviceId, hardware)
    }

    /**
     * 获取最新消息（简化示例）
     */
    fun getLatestAsk(deviceId: String): List<Map<String, Any?>> {
        val result = ubusRequest(deviceId, "nlp_result_get", "mibrain", emptyMap())
        println(result)
        if (result == null || (result["data"] as? Map<*, *>)?.get("code")?.toString()?.toIntOrNull() != 0) return emptyList()

        val infoStr = (result["data"] as Map<*, *>)["info"]?.toString() ?: return emptyList()
        val infoJson = infoStr.toMap()

//        val items = infoJson["result"]?.jsonArray ?: return emptyList()
//
        val messages = mutableListOf<Map<String, Any?>>()
//        for (item in items) {
//            val nlpStr = item.jsonObject["nlp"]?.jsonPrimitive?.content ?: continue
//            val nlpJson = Json.parseToJsonElement(nlpStr).jsonObject
//            val response = nlpJson["response"] ?: emptyMap<String, Any?>()
//            val meta = nlpJson["meta"]?.jsonObject
//            messages.add(
//                mapOf(
//                    "request_id" to meta?.get("request_id")?.jsonPrimitive?.content,
//                    "timestamp_ms" to meta?.get("timestamp")?.jsonPrimitive?.longOrNull,
//                    "response" to response
//                )
//            )
//        }
        return messages
    }

    /**
     * 文本转语音
     */
    fun textToSpeech(deviceId: String, text: String): Map<String, Any?>? {
        return ubusRequest(deviceId, "text_to_speech", "mibrain", mapOf("text" to text))
    }

    /**
     * 播放器控制
     */
    fun playerSetVolume(deviceId: String, volume: Int): Map<String, Any?>? {
        return ubusRequest(
            deviceId,
            "player_set_volume",
            "mediaplayer",
            mapOf("volume" to volume, "media" to "app_ios")
        )
    }

    fun playerPause(deviceId: String): Map<String, Any?>? {
        return ubusRequest(
            deviceId,
            "player_play_operation",
            "mediaplayer",
            mapOf("action" to "pause", "media" to "app_ios")
        )
    }

    fun playerStop(deviceId: String): Map<String, Any?>? {
        return ubusRequest(
            deviceId,
            "player_play_operation",
            "mediaplayer",
            mapOf("action" to "stop", "media" to "app_ios")
        )
    }

    fun playerPlay(deviceId: String): Map<String, Any?>? {
        return ubusRequest(
            deviceId,
            "player_play_operation",
            "mediaplayer",
            mapOf("action" to "play", "media" to "app_ios")
        )
    }

    fun playerGetStatus(deviceId: String): Map<String, Any?>? {
        return ubusRequest(deviceId, "player_get_play_status", "mediaplayer", mapOf("media" to "app_ios"))
    }

    fun playerSetLoop(deviceId: String, type: Int = 1): Map<String, Any?>? {
        return ubusRequest(deviceId, "player_set_loop", "mediaplayer", mapOf("media" to "common", "type" to type))
    }

    /**
     * 播放 URL 或音乐
     */
    fun playByUrl(deviceId: String, url: String, type: Int = 2): Map<String, Any?>? {
        if (!device2hardware.containsKey(deviceId)) {
            initDevices()
        }
        val hardware = device2hardware[deviceId]!!
        return if (usePlayMusicApi.contains(hardware)) {
            playByMusicUrl(deviceId, url, type = type)
        } else {
            ubusRequest(
                deviceId,
                "player_play_url",
                "mediaplayer",
                mapOf("url" to url, "type" to type, "media" to "app_ios")
            )
        }
    }

    /**
     * 初始化设备硬件映射
     */
    private fun initDevices() {
        val devices = deviceList()
        for (d in devices) {
            val deviceId = d.deviceID
            val hardware = d.hardware
            if (deviceId != null && hardware != null) {
                device2hardware[deviceId] = hardware
            }
        }
    }

    /**
     * 播放音乐 URL（兼容小米音乐 API）
     */
    fun playByMusicUrl(
        deviceId: String,
        url: String,
        type: Int = 1,
        audioId: String = "1696420661267792487",
        id: String = "355454500"
    ): Map<String, Any?>? {
        val audioType = if (type == 1) "MUSIC" else ""
        val music = mapOf(
            "payload" to mapOf(
                "audio_type" to audioType,
                "audio_items" to listOf(
                    mapOf(
                        "item_id" to mapOf(
                            "audio_id" to audioId,
                            "cp" to mapOf("album_id" to "-1", "episode_index" to 0, "id" to id, "name" to "xiaowei")
                        ),
                        "stream" to mapOf("url" to url)
                    )
                ),
                "list_params" to mapOf(
                    "listId" to "-1",
                    "loadmore_offset" to 0,
                    "origin" to "xiaowei",
                    "type" to "MUSIC"
                )
            ),
            "play_behavior" to "REPLACE_ALL"
        )
        return ubusRequest(
            deviceId,
            "player_play_music",
            "mediaplayer",
            mapOf("startaudioid" to audioId, "music" to music.json())
        )
    }

    fun playByMusicUrls(
        deviceId: String,
        type: Int = 1,
        startOffset: Int = 0,
        musics: List<XiaoMusicUrl>
    ): Map<String, Any?>? {
        val audioType = if (type == 1) "MUSIC" else ""
        val music = mapOf(
            "payload" to mapOf(
                "audio_type" to audioType,
                "audio_items" to musics.mapIndexed { index, music ->
                    mapOf(
                        "item_id" to mapOf(
                            "audio_id" to music.audioId,
                            "cp" to mapOf("album_id" to "-1", "episode_index" to 0, "id" to music.id, "name" to "xiaowei")
                        ),
                        "stream" to mapOf("url" to music.url),
                        "offset" to index
                    )
                },
                "list_params" to mapOf(
                    "listId" to "-1",
                    "loadmore_offset" to 0,
                    "origin" to "xiaowei",
                    "type" to "PLAYLIST"
                )
            ),
            "play_behavior" to "ENQUEUE",
            "needs_loadmore" to false
        )
        return ubusRequest(
            deviceId,
            "player_play_music",
            "mediaplayer",
            mapOf("startOffset" to startOffset, "startaudioid" to musics[startOffset].audioId, "music" to music.json())
        )
    }

    fun getAudioId(name: String): List<HashMap<String, Any?>> {

        val params = mutableMapOf<String, Any>(
            "query" to name,
            "queryType" to 1,
            "offset" to 0,
            "count" to 10,
            "timestamp" to Date().time / 1000,
        )

        val response = minaRequest("/music/search", params)

        val songList = (response?.get("data") as? Map<String, Any>)?.get("songList")?.toString().toModel<List<Map<String, Any>>>()
        val musicList = songList.map {
            hashMapOf(
                "id" to it["audioID"].toString(),
                "pic" to it["coverURL"].toString(),
                "name" to it["name"].toString(),
                "artist" to (it["artist"] as? Map<String, Any>)?.get("name"),
                "album" to it["albumName"].toString(),
            )
        }
//        println(musicList.json())
        return musicList
    }

    /**
     * 批量发送消息
     */
    suspend fun sendMessage(
        devices: List<Map<String, Any?>>,
        devno: Int,
        message: String?,
        volume: Int? = null
    ): Boolean {
        var result = false
        for ((i, device) in devices.withIndex()) {
            if (devno == -1 || devno != i + 1 || device["capabilities"]?.let { (it as Map<*, *>)["yunduantts"] == true } == true) {
                val deviceId = device["deviceID"]?.toString() ?: ""
                if (volume != null) {
                    result = playerSetVolume(deviceId, volume) != null
                } else {
                    result = true
                }
                if (result && message != null) {
                    result = textToSpeech(deviceId, message) != null
                }
                if (!result) {
                    println("Send failed: $message / $volume")
                }
                if (devno != -1 || !result) break
            }
        }
        return result
    }
}
