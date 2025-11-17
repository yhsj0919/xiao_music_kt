package xyz.yhsj.xyz.yhsj.cyf

data class CYFRespEntity(

    var status: Int? = null,
    var msg: CYFMsgEntity? = null

)

data class CYFMsgEntity(
    var songs: List<CYFMusicEntity>? = null,

    )


data class CYFMusicEntity(
    var id: String? = null,
    var title: String? = null,
    var author: String? = null,
    var url: String? = null,
    var pic: String? = null,
    var lrc: String? = null,

    )