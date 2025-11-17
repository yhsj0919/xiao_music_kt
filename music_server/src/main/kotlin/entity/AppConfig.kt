package xyz.yhsj.server.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import xyz.yhsj.server.validator.VG

@JsonIgnoreProperties(ignoreUnknown = true)

data class AppConfig(
    @field:NotBlank(message = "插件不可为空", groups = [VG.Add::class])
    var pluginId: String? = null,
    @field:NotBlank(message = "设备不可为空", groups = [VG.Add::class])

    var deviceID: String? = null,
    @field:NotEmpty(message = "响应词不可为空", groups = [VG.Add::class])
    var respWords: List<String>? = null,

    )