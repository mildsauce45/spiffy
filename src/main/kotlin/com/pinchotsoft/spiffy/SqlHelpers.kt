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
    // First let's expand any iterable parameters into the sql statement
    var (transformedSql, expandedParams) = handleIterableInputParams(sql, inputParams)

    var paramIndex = 1
    val transformedParams = HashMap<Int, Any?>()

    // Now let's search the the string for any parameter we've label either via a ':' or a '@' and replace them with ?s
    // so that the jdbc driver can properly accept them as PrepareStatement parameters
    var strSearchIdx = 0
    while (strSearchIdx < transformedSql.length) {
        val currChar = transformedSql[strSearchIdx]

        if (currChar == ':' || currChar == '@') {
            var paramEndIndex = strSearchIdx + 1

            while(paramEndIndex < transformedSql.length && transformedSql[paramEndIndex].isLetterOrDigit())
                paramEndIndex++

            // Get the parameter name so we can extract it from the input parameter map
            val paramName = transformedSql.substring(strSearchIdx + 1, paramEndIndex)

            val paramFromMap = expandedParams.keys.firstOrNull { it.equals(paramName, ignoreCase = true) }

            if (paramFromMap != null) {
                // Nulls are special cased here. Since I really don't want more reflection, and JDBC still needs to
                // know the nullable type, I'm going to keep the string replacement.
                val paramValue = expandedParams[paramFromMap]
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

private fun handleIterableInputParams(sql: String, inputParams: Map<String, Any?>): Pair<String, Map<String, Any?>> {
    var transformedSql = sql
    val transformedInputs = HashMap<String, Any?>()

    for ((key, value) in inputParams) {
        // We still need to handle null elements properly, since we're just expanding iterable parameters at the moment,
        // this just means forwarding the value up to the calling function to properly handle null values
        if (value == null) {
            transformedInputs.put(key, value)
        } else {
            val (resultSql, resultInputs) = transformIterableSql(transformedSql, key, value)

            transformedSql = resultSql
            for ((key2, value2) in resultInputs)
                transformedInputs.put(key2, value2)
        }
    }

    return Pair(transformedSql, transformedInputs)
}

inline fun <reified T> transformIterableSql(sql: String, key: String, obj: T): Pair<String, Map<String, Any?>> where T : Any {
    return GenericStringTransformer<Iterable<T>>().transform(obj) { o, isIterable ->
        if (isIterable) {
            // If the parameter is Iterable<T> then we need to collect new keys into our result params set, and replace
            // the old parameter in the sql string with the expanded version (e.g. @Ids -> (@Ids1, @Ids2, ..., @IdsN) )
            @Suppress("UNCHECKED_CAST")
            val listVer = (o as Iterable<T>).toList()
            val resultInputs = HashMap<String, Any?>()

            for (i in listVer.indices) {
                val newItemKey = "$key${i + 1}"
                resultInputs.put(newItemKey, listVer[i])
            }

            Pair(sql.replace(Regex("[@:]$key", RegexOption.IGNORE_CASE), resultInputs.keys.joinToString(prefix = "(", postfix = ")", separator = ",") { "@$it" }), resultInputs)
        } else {
            // Otherwise we can just pass the key and object back without modification
            // We do have to pass back a map, as the calling function will be adding all the parameters to a master list
            Pair(sql, mapOf(key to obj ))
        }
    }
}

inline fun <reified T> getStringValue(obj: T):String where T : Any {
    return GenericStringTransformer<Iterable<T>>().transformToString(obj) {
        val isQuotable = shouldQuote(T::class.java)

        @Suppress("UNCHECKED_CAST")
        (it as Iterable<T>).joinToString(prefix = "(", postfix = ")", separator = ",") { if (isQuotable) "'$it'" else "$it" }
    }
}




