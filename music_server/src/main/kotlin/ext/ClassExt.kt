package xyz.yhsj.server.ext

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.internal.KotlinReflectionInternalError


/**
 * 根据泛型新建对象
 */
inline fun <reified T : Any> new(vararg params: Any): T =
    T::class.java.getDeclaredConstructor(*params.map { it::class.java }.toTypedArray()).apply { isAccessible = true }
        .newInstance(*params)

/**
 * 获取指定注解
 */
inline fun <reified T : Annotation> Any.getAnno(): Map<String, T?> =
    try {
        val type: KClass<*> = this::class
        type.primaryConstructor?.parameters?.filter { it.findAnnotation<T>() != null }
            ?.associate { (it.name ?: "") to it.findAnnotation() } ?: hashMapOf()
    } catch (error: KotlinReflectionInternalError) {
        hashMapOf()
    }

/**
 * 获取指定注解的字段
 */
inline fun <reified T : Annotation> Any.getAnnoField(): List<KProperty1<Any, *>?> =

    try {
        val type: KClass<*> = this::class
        val parameter = type.primaryConstructor?.parameters?.filter { it.findAnnotation<T>() != null }
        parameter?.map { p ->
            type.memberProperties.firstOrNull {
                it.name == p.name
            } as KProperty1<Any, *>?
        } ?: arrayListOf()

    } catch (error: KotlinReflectionInternalError) {
        arrayListOf()
    }

