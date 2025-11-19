package xyz.yhsj.kuwo

data class KuWoRecommendEntity(

    var child: List<KuWoRecommendChildEntity>? = null

)

data class KuWoRecommendChildEntity(

    var child: List<KuWoMusicRecEntity>? = null

)

data class KuWoMusicRecEntity(
    var data: KuWoMusicRecDataEntity? = null,
)

data class KuWoMusicRecDataEntity(
    var rid: String? = null,
    var name: String? = null,
    var img: String? = null,
    var artist: String? = null,
    var album: String? = null,

    )