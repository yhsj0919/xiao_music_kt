package xyz.yhsj.server.entity.resp

/**
 * 根据传入的当前多少页
 *
 * @param size
 * @param page
 * @param totalElements
 */
class PageUtil(
    //当前页数
    page: Int,
    // 每页大小
    var size: Int,
    // 总共有多少条数据
    var totalElements: Long,

    content: List<Any>?,
) {

    /**
     * 当前页为第几页
     */
    var page: Int = 0

    /**
     * 是否为第一页
     */
    var isFirst = false

    /**
     * 是否为最后一页
     */
    var isLast = false

    /**
     * 总共有多少页
     */
    var totalPages: Long = 0
        private set

    /**
     * 当前页一共有多少条数据
     */
    var numberOfElements: Int = 0

    /**
     * 数据
     */
    var content: List<Any>? = null
        set(value) {
            this.numberOfElements = value?.size ?: 0
            field = value
        }

    init {

        this.page = if (page < 0) 0 else page

        this.totalPages = if (totalElements % size == 0L) totalElements / size else totalElements / size + 1

        this.isFirst = page == 0

        this.isLast = page + 1 >= this.totalPages

        this.content = content

    }


}