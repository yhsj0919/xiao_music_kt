package xyz.yhsj.server.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)

data class PluginInfo(
    var pluginId: String? = null,
    var version: String? = null,
    var provider: String? = null,
    var path: String? = null,
    var description: String? = null
)