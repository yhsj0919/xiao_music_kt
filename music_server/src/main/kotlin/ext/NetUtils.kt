package xyz.yhsj.server.ext

// NetUtils.kt
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.*

object NetUtils {
    /**
     * 返回第一个可用的 IPv4 地址（非 loopback），找不到则返回 null。
     */
    fun getRealLocalIp(): String? {
        try {
            val ifaces = NetworkInterface.getNetworkInterfaces() ?: return null
            for (iface in Collections.list(ifaces)) {
                if (!iface.isUp || iface.isLoopback || iface.isVirtual) continue
                val addrs = Collections.list(iface.inetAddresses)
                for (addr in addrs) {
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            // 可选：记录日志 e
        }
        return null
    }

    /**
     * 列出所有非回环的 IPv4 地址（可能有多个网卡）。
     */
    fun listAllLocalIps(): List<String> {
        val result = mutableListOf<String>()
        try {
            val ifaces = NetworkInterface.getNetworkInterfaces() ?: return result
            for (iface in Collections.list(ifaces)) {
                if (!iface.isUp || iface.isLoopback || iface.isVirtual) continue
                val addrs = Collections.list(iface.inetAddresses)
                for (addr in addrs) {
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        result.add(addr.hostAddress)
                    }
                }
            }
        } catch (e: Exception) {
            // 可选：记录日志 e
        }
        return result
    }
}
