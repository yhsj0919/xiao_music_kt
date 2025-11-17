package xyz.yhsj.server.api.apis

import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.koin.mp.KoinPlatform.getKoin
import org.pf4j.DefaultPluginManager
import org.pf4j.PluginManager
import org.pf4j.PluginWrapper
import xyz.yhsj.music_impl.Music
import xyz.yhsj.music_impl.MusicImpl
import xyz.yhsj.music_impl.toModel
import xyz.yhsj.server.APP_CONFIG
import xyz.yhsj.server.APP_HOST
import xyz.yhsj.server.APP_PORT
import xyz.yhsj.server.MI_PASS_WORD
import xyz.yhsj.server.MI_USER_NAME
import xyz.yhsj.server.entity.AppConfig
import xyz.yhsj.server.entity.SysAccount
import xyz.yhsj.server.entity.resp.CommonResp
import xyz.yhsj.server.entity.PluginInfo
import xyz.yhsj.server.ext.KeyValueStore
import xyz.yhsj.server.ext.getExt
import xyz.yhsj.server.ext.postExt
import xyz.yhsj.server.music.BackgroundTask
import xyz.yhsj.server.music.PluginsManager
import xyz.yhsj.server.validator.VG
import xyz.yhsj.xiao_music.MiAccount
import xyz.yhsj.xiao_music.MiNAService
import java.nio.file.Paths


fun Route.musicApi() {
    val backgroundTask: BackgroundTask by inject()
    val plugins: PluginsManager by inject()
    val account: MiAccount by inject()
    val store: KeyValueStore by inject()

    route("/server") {

        getExt("/test") { params, _ ->
            var config = store.get<AppConfig>(APP_CONFIG)
            val service = MiNAService(account)

            service.playByMusicUrl(deviceId = config?.deviceID ?: "", "https://67373.chatcyf.com/%E9%99%88%E4%B8%80%E5%8F%91%E5%84%BF%20-%20%E7%AB%A5%E8%AF%9D%E9%95%87.mp3")
            CommonResp.success()
        }

        getExt("/check") { params, _ ->
            try {
                val login = account.isLogin
                if (login) {
                    return@getExt CommonResp.success()

                } else {
                    return@getExt CommonResp.error(msg = "登录失败")
                }

            } catch (e: Exception) {


                return@getExt CommonResp.error(msg = "登录失败")
            }

        }
    }


    route("/music") {
        getExt("/url") { params, _ ->
            val pluginId = params["pluginId"]
            val music = params["params"].toModel<Music>()

            println("即将播放：$music")
            val musicPlugin = plugins.getExtensions(MusicImpl::class.java, pluginId).firstOrNull()

            if (musicPlugin != null) {

                val url = musicPlugin.url(music)
                if (url != null) {
                    println("重定向音乐=$url")
                    call.respondRedirect(url)

                } else {
                    println("没有可以重定向的音乐")
                    call.respondRedirect("")
                }
            }
        }
    }

    route("/setting") {
        postExt<SysAccount>("/login") { params, _ ->
            store.remove(MI_USER_NAME)
            store.remove(MI_PASS_WORD)
            backgroundTask.stop()
            account.cleanToken()
            account.username = params.username
            account.password = params.password
            try {
                val login = account.login("micoapi")
                return@postExt if (login) {
                    println("✅小米服务登录成功")

                    store.put(MI_USER_NAME, params.username)
                    store.put(MI_PASS_WORD, params.password)
                    store.put(APP_HOST, params.host)
                    store.put(APP_PORT, params.port)

                    backgroundTask.start(application)

                    CommonResp.success();

                } else {
                    println("🛑小米服务登录失败")
                    CommonResp.error(msg = "操作失败")
                }

            } catch (e: Exception) {
                CommonResp.error(msg = "操作失败")
                println("🛑用户名，密码不存在")
            }
            CommonResp.error(msg = "操作失败")

        }
        getExt("/deviceList") { params, _ ->

            val service = MiNAService(account)

            val deviceList = service.deviceList()
            CommonResp.success(data = deviceList)

        }

        getExt("/pluginList") { params, _ ->
            plugins.loadPlugins()
            val deviceList = plugins.getPlugins().map {
                PluginInfo(it.descriptor.pluginId, it.descriptor.version, it.descriptor.provider, it.pluginPath.toString(), it.descriptor.pluginDescription)
            }
            CommonResp.success(data = deviceList)
        }
        getExt("/getConfig") { params, _ ->
            val config = store.get<AppConfig>(APP_CONFIG) ?: AppConfig()
            CommonResp.success(data = config)
        }
        postExt<AppConfig>("/setConfig", VG.Add::class.java) { params, _ ->
            store.put(APP_CONFIG, params)
            backgroundTask.config = params
            backgroundTask.start(application)
            CommonResp.success()
        }

    }

    get("/") {
        call.respondText("Ktor 后台任务服务运行中。访问 /start 或 /stop 控制任务。")
    }

    get("/start") {
        backgroundTask.start(application)
        call.respondText("✅ 任务已启动")
    }

    get("/stop") {
        backgroundTask.stop()
        call.respondText("🛑 任务已停止")
    }

    get("/status") {
        call.respondText("任务状态: ${if (backgroundTask.isRunning()) "运行中" else "已停止"}")
    }
}
