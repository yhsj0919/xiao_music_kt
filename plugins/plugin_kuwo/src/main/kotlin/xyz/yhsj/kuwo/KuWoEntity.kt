package xyz.yhsj.kuwo

data class KuWoRespEntity(

    var abslist: List<KuWoMusicEntity>? = null

)

data class KuWoMusicEntity(
    var DC_TARGETID: String? = null,
    var SONGNAME: String? = null,
    var web_albumpic_short: String? = null,
    var ARTIST: String? = null,
    var ALBUM: String? = null,

    )