package xyz.yhsj.kuwo

import org.pf4j.Plugin
import org.pf4j.PluginWrapper

class KuWoPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    override fun start() {
        println("✅ HttpPlugin started!")
    }

    override fun stop() {
        println("🛑 HttpPlugin stopped!")
    }
}
