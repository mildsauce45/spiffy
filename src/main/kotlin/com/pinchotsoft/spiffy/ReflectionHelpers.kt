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
    return clazz.isPrimitive || ReflectionHelper.wrapperTypes.contains(clazz)
}

private class ReflectionHelper {
    companion object {
        val wrapperTypes: Set<Class<*>> = setOf(
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
    }
}

