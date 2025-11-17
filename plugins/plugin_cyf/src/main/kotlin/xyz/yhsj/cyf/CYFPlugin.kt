package xyz.yhsj.cyf

import org.pf4j.Plugin
import org.pf4j.PluginWrapper

class CYFPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    override fun start() {
        println("✅ 陈一发儿 started!")
    }

    override fun stop() {
        println("🛑 陈一发儿 stopped!")
    }
}
