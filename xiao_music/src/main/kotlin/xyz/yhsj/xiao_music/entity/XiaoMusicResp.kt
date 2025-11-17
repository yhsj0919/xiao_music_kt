package xyz.yhsj.xiao_music.entity

data class  XiaoMusicResp<T>(
    var code: Int? = null,
    var message: String? = null,
    var data: T? = null,
)