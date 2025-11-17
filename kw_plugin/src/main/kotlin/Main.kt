package xyz.yhsj.cyf

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {

    val kw = KuwoMusic()

    var musicList = kw.search("周杰伦")
    var ss = musicList.find { it["name"] == "最伟大" }
    if (ss != null) {
        kw.getUrl(ss["id"].toString())
    }else{
        println("no url")
    }

    println()
}