package com.pinchotsoft.spiffy

import java.lang.reflect.Field

fun <T> getField(fieldName: String, clazz: Class<T>): Field? {
    return clazz.declaredFields.firstOrNull { it.name.equals(fieldName, true) }
}

fun <T> getFieldValue(obj: T, fieldName: String, clazz: Class<T>): Any? {
    val field = getField(fieldName, clazz) ?: return null

    return getFieldValue(obj, field, clazz)
}

fun <T> getFieldValue(obj: T, field: Field, clazz: Class<T>): Any? {
    val getter = clazz.methods.firstOrNull { it.name.equals("get${field.name}", true) } ?: return null

    return getter.invoke(obj)
}

fun isPrimitive(clazz: Class<*>): Boolean {
    return clazz.isPrimitive || ReflectionHelper.WRAPPER_TYPES.contains(clazz)
}

fun getDefaultValue(clazz: Class<*>): Any? {
    return ReflectionHelper.DEFAULT_VALUES[clazz]
}

private class ReflectionHelper {
    companion object {
        val WRAPPER_TYPES: Set<Class<*>> = setOf(
                String::class.java,
                Boolean::class.java,
                Character::class.java,
                Byte::class.java,
                Short::class.java,
                Integer::class.java,
                Long::class.java,
                Float::class.java,
                Double::class.java,
                Void::class.java)

        val DEFAULT_VALUES: Map<Class<*>, Any?> = mapOf(
                String::class.java to "",
                Boolean::class.java to false,
                Character::class.java to ' ',
                Byte::class.java to 0,
                Short::class.java to 0,
                Integer::class.java to 0,
                Long::class.java to 0,
                Float::class.java to 0,
                Double::class.java to 0,
                Void::class.java to 0,
                Boolean::class.javaPrimitiveType!! to false,
                Char::class.javaPrimitiveType!! to ' ',
                Byte::class.javaPrimitiveType!! to 0,
                Short::class.javaPrimitiveType!! to 0,
                Int::class.javaPrimitiveType!! to 0,
                Long::class.javaPrimitiveType!! to 0,
                Float::class.javaPrimitiveType!! to 0,
                Double::class.javaPrimitiveType!! to 0)
    }
}

