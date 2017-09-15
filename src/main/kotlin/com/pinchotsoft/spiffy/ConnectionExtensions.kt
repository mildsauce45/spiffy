package com.pinchotsoft.spiffy

import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.util.Vector

fun <T> Connection.query(sql: String, parameters: Map<String, Any?>, clazz: Class<T>): List<T> {
    var localSql = sql
    if (parameters.count() > 0) {
        for ((key, value) in parameters)
            localSql = localSql.replace("@$key", value.toString())
    }

    return query(localSql, clazz)
}

fun <T> Connection.query(sql: String, template: T): List<T> where T : kotlin.Any {
    val extractRegex = Regex("@[a-zA-Z]+")

    val matches = extractRegex.findAll(sql)
    val clazz = template.javaClass

    var localSql = sql

    for (m in matches) {
        val field = getField(m.value.substring(1), clazz) ?: continue
        var fieldValue = getFieldValue(template, field, clazz)

        if (fieldValue != null) {
            fieldValue = if (shouldQuote(field)) "'$fieldValue'" else fieldValue.toString()

            localSql = localSql.replace(m.value, fieldValue)
        }
    }

    return query(localSql, clazz)
}

fun <T> Connection.query(sql: String, clazz: Class<T>): List<T> {
    var stmt: Statement? = null
    var rs: ResultSet? = null

    try {
        stmt = this.createStatement() ?: return emptyList()
        rs = stmt.executeQuery(sql) ?: return emptyList()

        val results = Vector<T>()

        while (rs.next()) {
            val m = mapModel(rs, clazz) ?: continue

            results.add(m)
        }
    } catch (e: Exception) {
        // Log something? Throw error?
    } finally {
        stmt?.close()
        rs?.close()
    }

    return emptyList()
}