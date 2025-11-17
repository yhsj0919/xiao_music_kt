package xyz.yhsj.xiao_music.entity

data class XiaoMusicDevice(
    var name: String? = null,
    var presence: String? = null,
    var miotDID: Long? = null,
    var hardware: String? = null,
    var deviceID: String? = null,
)