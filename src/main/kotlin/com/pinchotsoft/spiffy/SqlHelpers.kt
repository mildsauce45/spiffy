package com.pinchotsoft.spiffy

import java.lang.reflect.Field
import java.util.UUID
import com.pinchotsoft.spiffy.utilities.GenericStringTransformer

fun shouldQuote(fieldType: Field): Boolean {
    return shouldQuote(fieldType.type)
}

fun shouldQuote(clazz: Class<*>): Boolean {
    return clazz == String::class.java || clazz == UUID::class.java
}

fun jdbcEscape(sproc: String, inputParams: Map<String, Any?>): String {
    return if (inputParams.count() == 0)
        "{call $sproc}"
    else {
        val parmString = (inputParams.map { "?" }).joinToString(", ")

        "{call $sproc($parmString)}"
    }
}

fun transformSql(sql: String, inputParams: Map<String, Any?>): Pair<String, Map<Int, Any?>> {

    var paramIndex = 1
    var transformedSql = sql
    val transformedParams = HashMap<Int, Any?>()

    var strSearchIdx = 0
    while (strSearchIdx < transformedSql.length) {
        val currChar = transformedSql[strSearchIdx]

        if (currChar == ':' || currChar == '@') {
            var paramEndIndex = strSearchIdx + 1

            while(paramEndIndex < transformedSql.length && transformedSql[paramEndIndex].isLetterOrDigit())
                paramEndIndex++

            val paramName = transformedSql.substring(strSearchIdx + 1, paramEndIndex)

            val paramFromMap = inputParams.keys.firstOrNull { it.equals(paramName, ignoreCase = true) }

            if (paramFromMap != null) {
                // Nulls are special cased here. Since I really don't want more reflection, and JDBC still needs to
                // know the nullable type, I'm going to keep the string replacement.
                val paramValue = inputParams[paramFromMap]
                val replacementRegex = Regex("[@:]$paramName")

                if (paramValue == null) {
                    transformedSql = transformedSql.replace(replacementRegex, "NULL")
                } else {
                    transformedSql = transformedSql.replace(replacementRegex, "?")
                    transformedParams.put(paramIndex++, paramValue)
                }
            }
        }

        strSearchIdx++
    }

    return Pair(transformedSql, transformedParams)
}

inline fun <reified T> getStringValue(obj: T):String where T : Any {
    return GenericStringTransformer<Iterable<T>>().transformToString(obj) {
        val isQuotable = shouldQuote(T::class.java)

        @Suppress("UNCHECKED_CAST")
        (it as Iterable<T>).joinToString(prefix = "(", postfix = ")", separator = ",") { if (isQuotable) "'$it'" else "$it" }
    }
}




