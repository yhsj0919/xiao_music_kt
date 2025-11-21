package xyz.yhsj.server.plugins

import io.ktor.server.application.*
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import xyz.yhsj.server.APP_CONFIG
import xyz.yhsj.server.MI_PASS_WORD
import xyz.yhsj.server.MI_USER_NAME
import xyz.yhsj.server.entity.AppConfig
import xyz.yhsj.server.ext.KeyValueStore
import xyz.yhsj.server.ext.logger
import xyz.yhsj.server.music.MusicBackgroundTask
import xyz.yhsj.server.music.ReTryTask
import xyz.yhsj.xiao_music.MiAccount

/**
 * 系统初始化时运行
 */
fun Application.init() {
    val musicBackgroundTask: MusicBackgroundTask by inject()
    val reTryTask: ReTryTask by inject()
    val store: KeyValueStore by inject()
    val account: MiAccount by inject()


    val username = store.get<String>(MI_USER_NAME)
    val passWord = store.get<String>(MI_PASS_WORD)
    val config = store.get<AppConfig>(APP_CONFIG)
    account.username = username
    account.password = passWord

    if (username != null && passWord != null) {

        launch {
            try {
                val login = account.login()
                if (login) {

                    if (config?.deviceID != null) {
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

    launch {
        reTryTask.start(this)
    }

}
