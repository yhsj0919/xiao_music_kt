package xyz.yhsj.server.music

import kotlinx.coroutines.*
import org.koin.mp.KoinPlatform.getKoin
import org.pf4j.PluginWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.yhsj.music_impl.Music
import xyz.yhsj.music_impl.MusicImpl
import xyz.yhsj.music_impl.encodeUrl
import xyz.yhsj.server.APP_CONFIG
import xyz.yhsj.server.APP_HOST
import xyz.yhsj.server.APP_PORT
import xyz.yhsj.server.entity.AppConfig
import xyz.yhsj.server.ext.KeyValueStore
import xyz.yhsj.server.ext.containsIn
import xyz.yhsj.server.ext.json
import xyz.yhsj.server.ext.removeLongStart
import xyz.yhsj.server.ext.startIn
import xyz.yhsj.xiao_music.MiAccount
import xyz.yhsj.xiao_music.MiNAService
import xyz.yhsj.xiao_music.entity.XiaoMusicUrl
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean

// 定义后台任务类
class MusicBackgroundTask {
    val logger: Logger = LoggerFactory.getLogger(MusicBackgroundTask::class.java)

    private var job: Job? = null
    private val running = AtomicBoolean(false)

    val plugins: PluginsManager = getKoin().get()
    val account: MiAccount = getKoin().get()

    var miService: MiNAService = MiNAService(account)

    var lastTime: Long? = null
    var lastQuery: String? = null
    val store: KeyValueStore = getKoin().get()

    var config: AppConfig?
    var defRespWords = arrayListOf("陈一发", "放首歌", "每日推荐")

    constructor() {
        plugins.loadPlugins()
        plugins.startPlugins()

        // 打印插件信息
        plugins.getPlugins().forEach { plugin: PluginWrapper ->
            logger.info("Plugin: ${plugin.descriptor?.pluginId} - ${plugin.descriptor.version}")
        }

        config = store.get<AppConfig>(APP_CONFIG)

    }

    fun start(scope: CoroutineScope) {
        if (running.get()) {
            logger.info("任务已在运行中")
            return
        }
        val appHost = store.get<String>(APP_HOST)
        val appPort = store.get<Int>(APP_PORT) ?: 8080

        running.set(true)
        job = scope.launch {
            logger.info("后台任务已启动 ✅")
            while (isActive && running.get()) {
                try {
                    val musicPlugin = plugins.getExtensions(MusicImpl::class.java, config?.pluginId).firstOrNull()
                    val cyfPlugin = plugins.getExtensions(MusicImpl::class.java, "cyf-plugin").firstOrNull()

                    if (config != null && config?.deviceID != null && musicPlugin != null) {
                        val message = miService.getLatestAskFromXiaoAi(config?.deviceID!!)
                        if (message != null) {
                            val query = message["query"].toString()
                            val time = message["time"].toString().toLong()
                            if (lastTime != null && time > lastTime!! && (query.startIn(config?.respWords ?: arrayListOf()) != null || query.containsIn(defRespWords) != null)) {
                                logger.info("设备响应：$query")
                                logger.info("响应词：" + config?.respWords)
                                miService.playByUrl(config?.deviceID!!, "https://cdn.jsdelivr.net/gh/anars/blank-audio/1-second-of-silence.mp3")

                                try {


                                    if (query.contains("陈一发")) {
                                        if (cyfPlugin != null) {
                                            val musicList = cyfPlugin.recommend()
                                            val myList = musicList.map { music ->
                                                async(Dispatchers.IO) {
                                                    logger.info("最终播放歌曲：${music.title} - ${music.artist} - ${music.album}")
                                                    val currentId = 1696420661267792487L + (music.id?.toLong() ?: 1L)
                                                    XiaoMusicUrl(url = music.url?.encodeUrl() ?: "", audioId = currentId.toString())
                                                }
                                            }
                                            val results = myList.awaitAll()
                                            if (results.isNotEmpty()) {
                                                miService.playByMusicUrls(config?.deviceID!!, musics = results)
                                            } else {
                                                miService.textToSpeech(config?.deviceID!!, "没发现音乐")
                                            }
                                        } else {
                                            miService.textToSpeech(config?.deviceID!!, "音源不存在")
                                        }
                                    } else {
                                        val key = query.removeLongStart(config?.respWords ?: arrayListOf())
                                        logger.info("搜索词：$key")

                                        if (key.contains("放首歌") || key.contains("每日推荐")) {
                                            val musicList = musicPlugin.recommend().take(config?.maxSize ?: 20)
                                            logger.info("搜索到的音乐：$musicList")
                                            if (config?.respType == 1) {
                                                //搜索整个列表，但容易风控
                                                val myList = musicList.map { music ->
                                                    async(Dispatchers.IO) { matchFromXM(music, appHost, appPort) }
                                                }
                                                val results = myList.awaitAll()
                                                if (results.isNotEmpty()) {
                                                    miService.playByMusicUrls(config?.deviceID!!, musics = results)
                                                } else {
                                                    miService.textToSpeech(config?.deviceID!!, "没发现音乐")
                                                }
                                            } else {
                                                //搜索单曲
                                                val music = musicList.randomOrNull()
                                                if (music != null) {
                                                    val url = matchFromXM(music, appHost, appPort)
                                                    miService.playByMusicUrl(config?.deviceID!!, url.url, audioId = url.audioId)
                                                } else {
                                                    miService.textToSpeech(config?.deviceID!!, "没发现音乐")
                                                }
                                            }
                                        } else {
                                            val musicList = musicPlugin.search(key, config?.maxSize ?: 20).take(config?.maxSize ?: 20)
                                            logger.info("搜索到的音乐：$musicList")
                                            if (config?.respType == 1) {
                                                //搜索整个列表，但容易风控
                                                val myList = musicList.map { music ->
                                                    async(Dispatchers.IO) { matchFromXM(music, appHost, appPort) }
                                                }
                                                val results = myList.awaitAll()
                                                if (results.isNotEmpty()) {
                                                    miService.playByMusicUrls(config?.deviceID!!, musics = results)
                                                } else {
                                                    miService.textToSpeech(config?.deviceID!!, "没发现音乐")
                                                }
                                            } else {
                                                //搜索单曲
                                                val music = musicList.firstOrNull()
                                                if (music != null) {
                                                    val url = matchFromXM(music, appHost, appPort)
                                                    miService.playByMusicUrl(config?.deviceID!!, url.url, audioId = url.audioId)
                                                } else {
                                                    miService.textToSpeech(config?.deviceID!!, "没发现音乐")
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    logger.info("搜索时出现异常")
                                    e.printStackTrace()
                                }
                            }
                            lastTime = time
                            lastQuery = query
                        }
                    } else {
                        logger.info("插件或者小米设备Id不存在")
                    }

                    delay(1000)
                } catch (e: Exception) {
                    logger.info("后台任务异常: ${e.message}")
                }
            }
        }
    }

    /**
     * 匹配官方ID
     */
    fun matchFromXM(music: Music, host: String?, port: Int?): XiaoMusicUrl {
        logger.info("最终播放歌曲：${music.title} - ${music.artist} - ${music.album}")
        val list = miService.getAudioId(music.title?.replace("(Live)", "") ?: "")

        logger.info("搜索官方音乐：$list")

        val currentId =
            ((list.find {
                it["name"] == music.title && it["artist"].toString()
                    .replace("[^\\u4e00-\\u9fa5A-Za-z0-9]".toRegex(), "") == music.artist?.replace("[^\\u4e00-\\u9fa5A-Za-z0-9]".toRegex(), "")
            } ?: list.firstOrNull())?.get("id")
                ?: miService.defaultAudioId).toString()

        logger.info("匹配官方Id：$currentId")

        val params = music.json()

        val url =
            if (music.url != null) {
                music.url
            } else {
                "http://${host}:${port}/music/url?params=${
                    URLEncoder.encode(
                        params,
                        StandardCharsets.UTF_8
                    )
                }&pluginId=${config?.pluginId}"
            }

        logger.info("播放地址：$url")
        return XiaoMusicUrl(url = url!!, audioId = currentId)
    }


    fun stop() {
        if (!running.get()) {
            logger.info("任务未在运行")
            return
        }

        running.set(false)
        job?.cancel()
        job = null
        logger.info("后台任务已停止 🛑")
    }

    fun isRunning(): Boolean = running.get()
}