package xyz.yhsj.server.auth

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class AppSession(var token: String? = null, var time: Long = Date().time)





