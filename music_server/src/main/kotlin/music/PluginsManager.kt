package xyz.yhsj.server.music

import org.pf4j.DefaultPluginManager
import org.pf4j.PluginManager
import org.pf4j.PluginWrapper
import java.nio.file.Paths

class PluginsManager {
    var loaded = false
    val pluginsDir = if (System.getProperty("io.ktor.development")?.toBoolean() == true) {
        "./music_server/plugins"
    } else {
        "./plugins"
    }

    var pluginManager: PluginManager = DefaultPluginManager(Paths.get(pluginsDir))

    constructor() {
        println("插件管理器初始化")
        println("初始化地址：$pluginsDir")
    }

    fun loadPlugins() {
        if (!loaded) {
            loaded = true
            pluginManager.loadPlugins()
        }


    }

    fun startPlugins() {
        pluginManager.startPlugins()
    }

    fun getPlugin(pluginId: String): PluginWrapper? {
        return pluginManager.getPlugin(pluginId)
    }

    fun getPlugins(): List<PluginWrapper> {
        return pluginManager.plugins
    }

    fun <T> getExtensions(type: Class<T>): List<T> {
        return pluginManager.getExtensions(type)
    }

    fun <T> getExtensions(type: Class<T>, pluginId: String? = null): List<T> {
        return pluginManager.getExtensions(type, pluginId)
    }


    fun stopPlugins() {
        pluginManager.stopPlugins()
    }

    fun stopPlugin(pluginId: String) {
        pluginManager.stopPlugin(pluginId)
    }


}