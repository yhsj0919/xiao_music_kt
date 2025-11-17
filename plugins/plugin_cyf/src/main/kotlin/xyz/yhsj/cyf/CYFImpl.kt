package xyz.yhsj.cyf

import org.pf4j.Extension
import xyz.yhsj.music_impl.*
import xyz.yhsj.xyz.yhsj.cyf.CYFRespEntity
import java.net.URLEncoder

@Extension
class CYFImpl : MusicImpl {


    override fun search(key: String): List<Music> {

        val mainData = HttpUtils.get(
            "https://www.chatcyf.com/radio/",
            headers = hashMapOf(
                "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36 Edg/134.0.0.0",
            )
        )
        println(">>>>>>>>>>>>")
        val aa = mainData.body()
        val songsValue = Regex("""data-songs="([^"]+)"""")
            .find(aa)
            ?.groupValues?.get(1)

        val nonceValue = Regex("""data-_nonce="([^"]+)"""")
            .find(aa)
            ?.groupValues?.get(1)

        println(songsValue)
        println(nonceValue)
        val info = HttpUtils.get(
            "https://www.chatcyf.com/wp-admin/admin-ajax.php",
            params = hashMapOf(
                "action" to "hermit",
                "musicset" to songsValue.toString(),
                "_nonce" to nonceValue.toString(),
            ),
            headers = hashMapOf(
                "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36 Edg/134.0.0.0",
            )
        )

        val songList = info.body().toModel<CYFRespEntity>().msg?.songs

        val musicList = songList?.map {
            Music(
                id = it.id,
                title = it.title,
                pic = it.pic,
                artist = it.author,
                album = null,
                url = it.url,
            )
        } ?: emptyList()
        return musicList
    }


    override fun url(music: Music): String {
        return (music.url ?: "").encodeUrl()
    }

}