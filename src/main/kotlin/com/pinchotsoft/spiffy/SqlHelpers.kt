package com.pinchotsoft.spiffy

import java.lang.reflect.Field
import java.util.UUID

fun shouldQuote(fieldType: Field): Boolean {
    return shouldQuote(fieldType.type)
}

fun shouldQuote(clazz: Class<*>): Boolean {
    return clazz == String::class.java || clazz == UUID::class.java
}

fun jdbcEscape(sproc: String, inputParams: Map<String, Any?>): String {
    return if (inputParams.count() == 0)
        "{call ${sproc}}"
    else {
        val parmString = inputParams.map { "?" }.joinToString(", ")

        "{call ${sproc}(${parmString})}"
    }
}