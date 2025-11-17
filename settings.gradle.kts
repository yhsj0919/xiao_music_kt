plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "xiao_music_kt"
include("xiao_music")
include("kw_plugin")
include("music_server")
include("music_impl")
include("plugins")
include("plugins:plugin_kuwo")
include("plugins:plugin_cyf")