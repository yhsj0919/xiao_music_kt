package xyz.yhsj.xiao_music

import xyz.yhsj.xiao_music.entity.XiaoMusicUrl


fun main(args: Array<String>) {

//    val account = MiAccount("账号", "密码", "./mi_token.json")
//    val service = MiIOService(account)
//    println(service.deviceList())
//
//    service.miotAction("", 5 to 1, listOf("测试文本"))
//    val kw = KuwoMusic()
//
//    var musicList = kw.search("最伟大的作品")
//    var ss = musicList.find { it["name"].toString().contains("最伟大")  }
//    if (ss != null) {
//        kw.getUrl(ss["id"].toString())
//    }else{
//        println("no url")
//    }
    var lastTime: Long? = null
    var lastQuery: String? = null
    val deviceId = "你的设备ID";
    val account = MiAccount("账号", "密码", "./mi_token.json")
    val service = MiNAService(account)
//    println(service.playerGetStatus(deviceId))

    val ids = arrayListOf(
        "3422904591907291886",
        "1674531494303922261",
        "1674531697021977842",
        "1674530898931908808",
        "1674530704493411316",
        "1674518717132707105",
        "1754161764938817445",
        "3433051382495577463",
        "1674551168849694056",
        "1674537321322558464"
    )

    val musics = ids.map {
        XiaoMusicUrl(url = "http://lv.sycdn.kuwo.cn/e5c344aa64f50281b85eadfb0d611dd9/69146331/resource/30106/trackmedia/M800001yeNa60aLoOu.mp3?bitrate\$320&format\$mp3&source\$kwplayer_ar_5.1.0.0_B_jiakong_vh.apk&type\$convert_url2&user\$0&loginUid\$0", audioId = it)
    }

    service.playByMusicUrls(deviceId, musics = musics, startOffset = 3)


//    val kw = KuwoMusic()
//
//    val scheduler = Executors.newSingleThreadScheduledExecutor()
//
//    scheduler.scheduleAtFixedRate({
//        try {
//
//            val ss = service.getLatestAskFromXiaoAi(deviceId)
//            if (ss != null) {
//                val query = ss["query"].toString()
//                val time = ss["time"].toString().toLong()
//
//                if (lastTime != null && time > lastTime!! && query.startsWith("我想听")) {
//                    service.playByUrl(deviceId, "https://cdn.jsdelivr.net/gh/anars/blank-audio/1-second-of-silence.mp3")
//
//                    val key = query.replace("我想听", "")
//
//                    val musicList = kw.search(key)
//
//                    val ss = musicList.firstOrNull()
//                    if (ss != null) {
//                        println(ss["name"].toString())
//                        val list = service.getAudioId(ss["name"].toString())
//                        val currentId =
//                            ((list.find { it["name"] == ss["name"] && it["artist"] == ss["artist"] } ?: list.firstOrNull())?.get("id") ?: service.defaultAudioId).toString()
//                        val url = kw.getUrl(ss["id"].toString())
//                        if (url != null) {
//                            service.playByMusicUrl(deviceId, url, audioId = currentId)
//
//                        } else {
//                            service.textToSpeech(deviceId, "地址获取失败")
//                        }
//
//
//                    } else {
//                        service.textToSpeech(deviceId, "没发现音乐")
//
//                    }
//                } else {
//
////                println("time out: ${System.currentTimeMillis()}")
//                }
//
//                lastTime = time
//                lastQuery = query
//            }
////        println(ss)
//        } catch (e: Exception) {
//            println("循环错误:" + e.message)
//
//        }
//    }, 0, 1, TimeUnit.SECONDS)


}

