package admin

import org.junit.Assert
import java.lang.reflect.Field
import kotlin.reflect.KClass

inline fun <reified T> T.injectInto(parent: Any, name: String = ""): T {
    val type = when (this) {
        is Int -> Int::class.javaPrimitiveType
        is Float -> Float::class.javaPrimitiveType
        is Double -> Double::class.javaPrimitiveType
        is Short -> Short::class.javaPrimitiveType
        else -> T::class.java
    }
    val field = parent::class.java.findField(type!!, name)
    parent.setField(field.name, this, type)
    return this
}

inline fun <reified T> T.injectInto(parent: Any, type: Class<*>): T {
    val field = parent::class.java.findField(type, "")
    parent.setField(field.name, this, field.type)
    return this
}

inline fun <reified T> Any.getField(name: String): T {
    return getField(name, T::class.java)
}

//TODO
inline fun <reified C, reified T> getStaticValue(name: String): T {
    val field = C::class.java.findField(T::class.java, name)
    return field.runWithAccess {
        get(null) as T
    }
}

inline fun <reified T> Any.getJavaPrimitiveField(name: String, type: Class<*>): T {
    return getField(name, type)
}

inline fun <reified T> Any.setField(name: String, value: T?) {
    setField(name, value, T::class.java)
}

@SuppressWarnings("deprecation")
inline fun <reified T> Any.getField(name: String, type: Class<*>): T {
    return javaClass.findField(type, name).runWithAccess {
        get(this@getField) as T
    }
}

@SuppressWarnings("deprecation")
inline fun <reified T> Any.setJavaPrimitiveField(name: String, value: T) {
    val javaPrimitiveType = when (value) {
        is Int -> Int::class.javaPrimitiveType
        is Float -> Float::class.javaPrimitiveType
        is Double -> Double::class.javaPrimitiveType
        is Short -> Short::class.javaPrimitiveType
        else -> throw Exception("value is not a have an equivalent Java primitive type")
    }

    javaClass.findField(javaPrimitiveType!!, name).runWithAccess {
        set(this@setJavaPrimitiveField, value)
    }
}

fun Class<*>.findField(type: Class<*>, name: String = ""): Field {
    try {
        return declaredFields.firstOrNull {
            val wasAccessible = it.isAccessible
            try {
                it.isAccessible = true
                (name.isBlank() || it.name == name) && (it.type == type)
            } finally {
                it.isAccessible = wasAccessible
            }
        } ?: superclass!!.findField(type, name)
    } catch (e: Exception) {
        throw Exception("Class field $name with type $type does not exist")
    }
}

inline fun <reified T> Any.primitiveValue(type: KClass<*>, name: String = ""): T {
    return javaClass.findField(type.javaPrimitiveType!!, name).let {
        val wasAccessible = it.isAccessible
        it.isAccessible = true
        val result = it.get(this)
        it.isAccessible = wasAccessible
        result as T
    }
}


@SuppressWarnings("deprecation")
inline fun <reified T> Any.setField(name: String, value: T, type: Class<*>?) {
    javaClass.findField(type!!, name).runWithAccess {
        set(this@setField, value)
    }
}

var Any.outerClass: Any
    get() = javaClass.superclass
    @SuppressWarnings("deprecation")
    set(value) {
        javaClass.getDeclaredField("this$0").runWithAccess {
            set(this@outerClass, value)
        }
    }

fun Any.reflectiveEquals(expected: Any): Boolean {
    val fields = javaClass.declaredFields
    val expectedFields = expected.javaClass.declaredFields
    Assert.assertEquals(expectedFields.size, fields.size)
    for (i in 0..fields.lastIndex) {
        if (fields[i] != expectedFields[i]) {
            return false
        }
    }

    return true
}

inline fun <T> Field.runWithAccess(block: Field.() -> T): T {
    val wasAccessible = isAccessible
    isAccessible = true
    val result = block()
    isAccessible = wasAccessible
    return result
}

