package com.pinchotsoft.spiffy

import java.lang.reflect.Constructor
import java.sql.ResultSet

fun <T> mapModel(rs: ResultSet, clazz: Class<T>): T {
    if (isPrimitive(clazz)) {
        @Suppress("UNCHECKED_CAST")
        return rs.getObject(1) as T
    }

    val aConstructor = clazz.constructors.firstOrNull { it.parameters.isEmpty() }

    return if (aConstructor != null)
        mapPojo(rs, aConstructor, clazz)
    else
        mapDataClass(rs, clazz)
}

private fun <T> mapPojo(rs: ResultSet, constructor: Constructor<*>, clazz: Class<T>): T {
    val a = constructor.newInstance()

    for (f in clazz.declaredFields) {
        try {
            val column = rs.getObject(f.name) ?: continue

            val setter = clazz.methods.firstOrNull { it.name.equals("set${f.name}", true) }

            setter?.invoke(a, column)
        } catch (e: Exception) {
            // This is normal, not everything will necessarily be in the result set and vice-versa
        }
    }

    @Suppress("UNCHECKED_CAST")
    return a as T
}

private fun <T> mapDataClass(rs: ResultSet, clazz: Class<T>): T {
    val aConstructor = clazz.constructors.first()

    val argNames = clazz.declaredFields.map { it.name }

    val constructorArgs = ArrayList<Any?>()

    argNames.mapTo(constructorArgs) { rs.getObject(it) }

    @Suppress("UNCHECKED_CAST")
    return aConstructor.newInstance(*(constructorArgs.toArray())) as T
}