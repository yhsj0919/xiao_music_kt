package xyz.yhsj.server.plugins


import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import xyz.yhsj.server.ext.KeyValueStore
import xyz.yhsj.server.music.BackgroundTask
import xyz.yhsj.server.music.PluginsManager
import xyz.yhsj.xiao_music.MiAccount


fun Application.configureKoin() {
    //模块依赖注入
    install(Koin) {

        modules(koinModule)

    }
}


val koinModule = module {
    single { KeyValueStore() } // 单例注入
    single { MiAccount() } // 单例注入
    single { PluginsManager() } // 单例注入
    single { BackgroundTask() } // 单例注入


//    single<ComputerService> { ComputerServiceImpl() }
}