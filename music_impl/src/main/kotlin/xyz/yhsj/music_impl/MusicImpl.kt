package xyz.yhsj.music_impl

import org.pf4j.ExtensionPoint


interface MusicImpl : ExtensionPoint {
    /**
     * 每日推荐
     */
    fun recommend(): List<Music>

    /**
     * 搜索
     */
    fun search(key: String, size: Int?=20): List<Music>

    /**
     * 获取播放地址
     */
    fun url(music: Music): String?
}