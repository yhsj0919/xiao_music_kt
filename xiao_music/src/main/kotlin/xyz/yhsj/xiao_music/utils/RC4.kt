package xyz.yhsj.xiao_music.utils

class RC4(key: ByteArray) {
    private val s = ByteArray(256)
    private var i = 0
    private var j = 0

    init {
        for (k in 0..255) s[k] = k.toByte()
        var j = 0
        for (k in 0..255) {
            j = (j + s[k].toUByte().toInt() + key[k % key.size].toUByte().toInt()) and 0xFF
            s[k] = s[j].also { s[j] = s[k] }
        }
    }

    fun process(data: ByteArray): ByteArray {
        val out = ByteArray(data.size)
        for (k in data.indices) {
            i = (i + 1) and 0xFF
            j = (j + s[i].toUByte().toInt()) and 0xFF
            s[i] = s[j].also { s[j] = s[i] }
            val t = (s[i].toUByte().toInt() + s[j].toUByte().toInt()) and 0xFF
            out[k] = (data[k].toInt() xor s[t].toUByte().toInt()).toByte()
        }
        return out
    }
}
