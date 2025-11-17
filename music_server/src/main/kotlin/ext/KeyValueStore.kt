package xyz.yhsj.server.ext

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class KeyValueStore(private val filePath: String = "./config.json") {

    val gson = Gson()
    val storage = ConcurrentHashMap<String, JsonElement>()  // 内存缓存
    private val mutex = Mutex()  // 写锁
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var pendingSaveJob: Job? = null
    private val saveDelay: Duration = 200.milliseconds

    init {
        runBlocking { loadFromFile() }
    }

    /** 存储任意可序列化对象 */
    fun <T> put(key: String, value: T) {
        storage[key] = gson.toJsonTree(value)
        scheduleSave()
    }

    /** 获取对象，如果不存在返回 null */
    inline fun <reified T> get(key: String): T? {
        val element = storage[key] ?: return null
        return try {
            gson.fromJson(element, object : TypeToken<T>() {}.type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** 获取对象，如果不存在返回默认值 */
    inline fun <reified T> getOrDefault(key: String, default: T): T {
        return get(key) ?: default
    }

    fun remove(key: String) {
        storage.remove(key)
        scheduleSave()
    }

    fun contains(key: String): Boolean = storage.containsKey(key)

    fun clear() {
        storage.clear()
        scheduleSave()
    }

    fun keys(): Set<String> = storage.keys

    /** 延迟保存文件（防抖） */
    private fun scheduleSave() {
        pendingSaveJob?.cancel()
        pendingSaveJob = scope.launch {
            delay(saveDelay)
            saveToFile()
        }
    }

    /** 实际保存文件 */
    private suspend fun saveToFile() {
        mutex.withLock {
            try {
                val file = Paths.get(filePath).toFile()
                file.parentFile?.mkdirs()
                val jsonString = gson.toJson(storage)
                file.writeText(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** 从文件加载数据 */
    private suspend fun loadFromFile() {
        mutex.withLock {
            try {
                val file = Paths.get(filePath).toFile()
                if (file.exists()) {
                    val text = file.readText()
                    val jsonObj = JsonParser.parseString(text).asJsonObject
                    for ((k, v) in jsonObj.entrySet()) {
                        storage[k] = v
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** 关闭协程作用域 */
    fun close() {
        scope.cancel()
    }
}
