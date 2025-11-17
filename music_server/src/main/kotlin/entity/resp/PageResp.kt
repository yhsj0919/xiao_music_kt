package xyz.yhsj.server.entity.resp

import java.io.Serializable

//分页信息
data class PageResp(
        //第一页
        val first: Boolean = false,
        //最后一页
        val last: Boolean = false,
        //当前页数
        val page: Int = 0,
        //请求的条数
        val size: Int = 0,
        //当页条数
        val number: Int = 0,
        //总页数
        val totalPages: Int = 0,
        //总条数
        val totalSize: Long = 0
) : Serializable