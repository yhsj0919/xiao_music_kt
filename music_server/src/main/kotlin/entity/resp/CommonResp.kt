package xyz.yhsj.server.entity.resp


import xyz.yhsj.server.ext.json
import java.io.Serializable


class CommonResp(
    val code: Int = 200,
    val msg: String = "操作成功",
    data: Any? = null,
    var obj: Any? = null,
    var page: PageResp? = null,
) : Serializable {
    var data: Any? = null
        set(value) {
            field = when (value) {
                is PageUtil -> {
                    page = PageResp(
                        first = value.isFirst,
                        last = value.isLast,
                        page = value.page + 1,
                        size = value.size,
                        number = value.numberOfElements,
                        totalPages = value.totalPages.toInt(),
                        totalSize = value.totalElements
                    )
                    value.content ?: ArrayList<Any>()
                }

                else -> value
            }
        }

    init {
        this.data = data
    }

    companion object {
        @JvmStatic
        fun success(msg: String? = null, data: Any? = null, obj: Any? = null): CommonResp {
            return if (msg != null) {
                CommonResp(msg = msg, data = data, obj = obj)
            } else {
                CommonResp(data = data, obj = obj)
            }
        }

        @JvmStatic
        fun error(code: Int = 500, msg: String): CommonResp = CommonResp(code = code, msg = msg)

        @JvmStatic
        fun notFound(code: Int = 404, msg: String): CommonResp = CommonResp(code = code, msg = msg)

        @JvmStatic
        fun login(code: Int = 403, msg: String = "需要登录"): CommonResp = CommonResp(code = code, msg = msg)

        @JvmStatic
        fun empty(msg: String = "暂无数据"): CommonResp = CommonResp(msg = msg)

        @JvmStatic
        fun def(code: Int = 200, msg: String = "操作成功", data: Any? = null, obj: Any? = null): CommonResp =
            CommonResp(code = code, msg = msg, data = data, obj = obj)

    }


    override fun toString(): String {
        return this.json()
    }
}
