package xyz.yhsj.kuwo

import org.pf4j.Extension
import xyz.yhsj.music_impl.*

@Extension
class KuWoImpl : MusicImpl {
    /**
     * 每日推荐
     */
    override fun recommend(): List<Music> {
        val headers = hashMapOf(
            "referer" to "http://www.kuwo.cn/",
            "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 Edg/142.0.0.0",
        )
        // 定义查询参数
        val params = hashMapOf(
            "num" to "20",
            "source" to "kwplayer_ar_11.1.8.2_newpcguanwangmobile.apk",
            "type" to "rcm_discover",
            "uid" to 2803814035,
            "loginUid" to 0,
        )

        val ss = HttpUtils.get(
            "http://wapi.kuwo.cn/openapi/v1/recommend/daily/main",
            params,
            headers
        )

        val absList = ss.body().toModel<KuWoRecommendEntity>().child?.firstOrNull()?.child
        val musicList = absList?.map {
            Music(
                id = it.data?.rid,
                title = it.data?.name,
                pic = it.data?.img,
                artist = it.data?.artist,
                album = it.data?.album,
                url = null
            )
        } ?: emptyList()
//        println(musicList)
        return musicList
    }


    override fun search(key: String, size: Int?): List<Music> {
        val headers = hashMapOf(
            "referer" to "http://www.kuwo.cn/",
            "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 Edg/142.0.0.0",
        )
        // 定义查询参数
        val params = hashMapOf(
            "vipver" to "1",
            "ft" to "music",
            "encoding" to "utf8",
            "issubtitle" to "1",
            "mobi" to "1",
            "all" to key,
            "pn" to 0,
            "rn" to (size ?: 20)
        )

        val ss = HttpUtils.get(
            "https://www.kuwo.cn/search/searchMusicBykeyWord",
            params,
            headers
        )

        val absList = ss.body().toModel<KuWoRespEntity>().abslist
        val musicList = absList?.map {
            Music(
                id = it.DC_TARGETID,
                title = it.SONGNAME,
                pic = "https://img2.kuwo.cn/star/albumcover/${it.web_albumpic_short}",
                artist = it.ARTIST,
                album = it.ALBUM,
                url = null
            )
        } ?: emptyList()
//        println(musicList)
        return musicList
    }

    val first: String = "ylzsxkwm"

    private fun encode(id: String): String {
        val s =
            "user=0&android_id=0&prod=kwplayer_ar_5.1.0.0_B&corp=kuwo&newver=3&vipver=5.1.0.0&source=kwplayer_ar_5.1.0.0_B_jiakong_vh.apk&p2p=1&notrace=0&type=convert_url2&br=320kmp3&format=flac|mp3|aac&sig=0&rid=${id}&priority=bitrate&loginUid=0&network=WIFI&loginSid=0&mode=download"
        val bArr = s.toByteArray()
        val a2: ByteArray = d.a(bArr, bArr.size, first.toByteArray(), first.toByteArray().size)
        return String(b.a(a2, a2.size))
    }


    override fun url(music: Music): String? {
        val downloadUrl = "http://nmobi.kuwo.cn/mobi.s?f=kuwo&q=${encode(music.id ?: "")}"


        val data = HttpUtils.get(downloadUrl).body()

        var myUrl: String? = null
        data.split("\r\n").forEach {
            val item = it.split("=")
            if (item[0] == "url") {
                myUrl = item[1]
                return@forEach
            }
        }
//        println(myUrl)
        return myUrl
    }
}