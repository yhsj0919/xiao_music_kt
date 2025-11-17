package xyz.yhsj.server.validator


import jakarta.validation.Validation
import org.hibernate.validator.HibernateValidator
import java.util.*
import jakarta.validation.groups.Default


/**
 * 校验工具
 */
object ValidationUtils {
    private val validator =
        Validation
            .byProvider(HibernateValidator::class.java)
            .configure()
            .failFast(false)
            .buildValidatorFactory()
            .validator

    fun <T> validateEntity(obj: T?, vararg groups: Class<*>): ValidationResult {
        val result = ValidationResult()

        if (obj == null) {
            return result
        }
        val set = validator.validate(obj, *groups)
        if (set.isNotEmpty()) {
            result.hasErrors = true
            val errorMsg = HashMap<String, String>()
            for (cv in set) {
                errorMsg[cv.propertyPath.toString()] = cv.message
            }
            result.errorMsg = errorMsg
        }
        return result
    }

    fun <T> validateList(obj: List<T>?, vararg groups: Class<*>): ValidationResult {

        if (obj.isNullOrEmpty()) {
            return ValidationResult()
        }
        val res = obj.map {
            val result = ValidationResult()
            val set = validator.validate(it, *groups)
            if (set.isNotEmpty()) {
                result.hasErrors = true
                val errorMsg = HashMap<String, String>()
                for (cv in set) {
                    errorMsg[cv.propertyPath.toString()] = cv.message
                }
                result.errorMsg = errorMsg
            }
            result
        }.find { it.hasErrors }

        return res ?: ValidationResult()
    }


    fun <T> validateProperty(obj: T, propertyName: String): ValidationResult {
        val result = ValidationResult()
        val set = validator.validateProperty(obj, propertyName, Default::class.java)
        if (set.isNotEmpty()) {
            result.hasErrors = true
            val errorMsg = HashMap<String, String>()
            for (cv in set) {
                errorMsg[propertyName] = cv.message
            }
            result.errorMsg = errorMsg
        }
        return result
    }
}


