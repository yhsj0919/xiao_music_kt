package xyz.yhsj.server.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SysAccount(var username: String = "", var password: String = "", var host: String = "", var port: Int = 8080)