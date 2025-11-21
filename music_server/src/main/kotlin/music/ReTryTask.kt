package xyz.yhsj.server.music

import kotlinx.coroutines.*
import org.koin.mp.KoinPlatform.getKoin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.yhsj.server.APP_CONFIG
import xyz.yhsj.server.MI_PASS_WORD
import xyz.yhsj.server.MI_USER_NAME
import xyz.yhsj.server.entity.AppConfig
import xyz.yhsj.server.ext.KeyValueStore
import xyz.yhsj.xiao_music.MiAccount
import xyz.yhsj.xiao_music.MiNAService
import java.util.concurrent.atomic.AtomicBoolean

// 这个处理重新登录
class ReTryTask {
    private var job: Job? = null
    private val running = AtomicBoolean(false)

    val store: KeyValueStore = getKoin().get()
    val account: MiAccount = getKoin().get()
    val musicBackgroundTask: MusicBackgroundTask = getKoin().get()

    val logger: Logger = LoggerFactory.getLogger(ReTryTask::class.java)

    var miService: MiNAService = MiNAService(account)


    var config: AppConfig?

    constructor() {

        config = store.get<AppConfig>(APP_CONFIG)

    }

    fun start(scope: CoroutineScope) {
        if (running.get()) {
            logger.info("重试服务已在运行中")
            return
        }


        running.set(true)
        job = scope.launch {
            logger.info("重试服务已启动 ✅")
            while (isActive && running.get()) {
                delay(60 * 60 * 1000)

                try {
                    try {
                        val devices = miService.deviceList()
                        logger.info("服务正常不用重启")
                    } catch (e: Exception) {
                        logger.info("服务异常重启")
                        val username = store.get<String>(MI_USER_NAME)
                        val passWord = store.get<String>(MI_PASS_WORD)
                        val config = store.get<AppConfig>(APP_CONFIG)
                        account.username = username
                        account.password = passWord

                        if (username != null && passWord != null) {
                            try {
                                val login = account.login()
                                if (login) {

                                    if (config?.deviceID != null) {
                                        musicBackgroundTask.stop()
                                        musicBackgroundTask.start(this)
                                    }
                                    logger.info("✅小米服务登录成功")
                                } else {
                                    logger.info("🛑小米服务登录失败")
                                }
                            } catch (e: Exception) {
                                logger.info("🛑用户名，密码不存在")
                            }
                        }
                    }

                } catch (e: Exception) {
                    logger.info("重试服务异常: ${e.message}")
                }


            }
        }
    }


    fun stop() {
        if (!running.get()) {
            logger.info("重试服务未在运行")
            return
        }

        running.set(false)
        job?.cancel()
        job = null
        logger.info("重试服务已停止 🛑")
    }

    fun isRunning(): Boolean = running.get()
}