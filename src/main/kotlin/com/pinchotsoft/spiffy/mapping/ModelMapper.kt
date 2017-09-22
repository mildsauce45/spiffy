package com.pinchotsoft.spiffy.mapping

import com.pinchotsoft.spiffy.ReflectionHelper
import com.pinchotsoft.spiffy.getDefaultValue
import java.lang.reflect.Constructor
import java.sql.ResultSet

fun <T> mapModel(rs: ResultSet, context: ResultContext, clazz: Class<T>): T {
    if (context.isPrimitiveType) {
        @Suppress("UNCHECKED_CAST")
        return rs.getObject(1) as T
    }

    return if (!context.isDataClass)
        mapPojo(rs, context.constructor!!, clazz)
    else
        mapDataClass(rs, context, clazz)
}

private fun <T> mapPojo(rs: ResultSet, constructor: Constructor<*>, clazz: Class<T>): T {
    val a = constructor.newInstance()

    for (f in ReflectionHelper.getFieldsForClass(clazz)) {
        try {
            val column = rs.getObject(f.name) ?: continue

            val setter = ReflectionHelper.getMethodsForClass(clazz).firstOrNull { it.name.equals("set${f.name}", true) }

            setter?.invoke(a, column)
        } catch (e: Exception) {
            // This is normal, not everything will necessarily be in the result set and vice-versa
        }
    }

    @Suppress("UNCHECKED_CAST")
    return a as T
}

private fun <T> mapDataClass(rs: ResultSet, context: ResultContext, clazz: Class<T>): T {
    val constructorArgs = ArrayList<Any?>()

    for (field in ReflectionHelper.getFieldsForClass(clazz)) {
        val hasMatch = context.hasColumn(field.name)

        if (hasMatch)
            constructorArgs.add(rs.getObject(field.name))
        else
            constructorArgs.add(getDefaultValue(field.type))
    }

    @Suppress("UNCHECKED_CAST")
    return context.constructor!!.newInstance(*(constructorArgs.toArray())) as T
}