package com.pinchotsoft.spiffy

import java.lang.reflect.Field
import java.util.UUID

fun shouldQuote(fieldType: Field): Boolean {
    val type = fieldType.type

    return type == String::class.java || type == UUID::class.java
}