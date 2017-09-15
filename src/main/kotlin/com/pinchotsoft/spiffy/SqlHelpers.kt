package com.pinchotsoft.spiffy

import java.lang.reflect.Field
import java.util.UUID

fun shouldQuote(fieldType: Field): Boolean {
    return shouldQuote(fieldType.type)
}

fun shouldQuote(clazz: Class<*>): Boolean {
    return clazz == String::class.java || clazz == UUID::class.java
}