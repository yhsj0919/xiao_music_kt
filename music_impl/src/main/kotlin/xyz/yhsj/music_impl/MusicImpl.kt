package xyz.yhsj.music_impl

import org.pf4j.ExtensionPoint


interface MusicImpl : ExtensionPoint {
     fun search(key: String): List<Music>
     fun url(music: Music): String?
}