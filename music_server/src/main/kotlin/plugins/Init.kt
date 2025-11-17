package xyz.yhsj.server.plugins

import io.ktor.server.application.*
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import xyz.yhsj.server.APP_CONFIG
import xyz.yhsj.server.MI_PASS_WORD
import xyz.yhsj.server.MI_USER_NAME
import xyz.yhsj.server.entity.AppConfig
import xyz.yhsj.server.ext.KeyValueStore
import xyz.yhsj.server.music.BackgroundTask
import xyz.yhsj.xiao_music.MiAccount

/**
 * 系统初始化时运行
 */
fun Application.init() {
    val backgroundTask: BackgroundTask by inject()
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
                        backgroundTask.start(this)
                    }
                    println("✅小米服务登录成功")

                } else {
                    println("🛑小米服务登录失败")
                }

            } catch (e: Exception) {

                println("🛑用户名，密码不存在")
            }
        }
    }


}
