package com.pinchotsoft.spiffy

import java.sql.*
import java.util.Vector

/**
 * Executes the given sql, after substituting the values in the map. The replacement is case-insensitive.
 * Returns a list of the given type from the result set.
 */
fun <T> Connection.query(sql: String, parameters: Map<String, Any?>, clazz: Class<T>, commandType: CommandType = CommandType.TEXT): List<T> {
    return if (commandType == CommandType.TEXT)
        query(insertMapValues(sql, parameters), clazz, commandType)
    else
        executeStoredProcWithResults(this, sql, parameters, clazz)
}

/**
 * Executes the given sql, substituting any matching values found on the given template object. The name matching is case-insensitive.
 * Returns a list of the given type from the result set.
 */
fun <T> Connection.query(sql: String, template: T, commandType: CommandType = CommandType.TEXT): List<T> where T : kotlin.Any {
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

    return query(localSql, clazz, commandType)
}

/**
 * Executes the given sql and returns a list of the given type from the result set
 */
fun <T> Connection.query(sql: String, clazz: Class<T>, commandType: CommandType = CommandType.TEXT): List<T> {
    return if (commandType == CommandType.TEXT)
        executeTextCommandWithResults(this, sql, clazz)
    else
        executeStoredProcWithResults(this, sql, emptyMap(), clazz)
}

fun Connection.query(sql:String, parameters: Map<String, Any?>? = null, commandType: CommandType = CommandType.TEXT): List<Map<String, Any?>> {
    return if (commandType == CommandType.TEXT)
        executeTextCommandWithResults(this, insertMapValues(sql, parameters))
    else
        executeStoredProcWithResults(this, sql, parameters ?: emptyMap())
}

/**
 * Executes the given sql after substituting the values in the map. The replacement is case-insensitive.
 * Will return null if a Statement was not created or an exception occurred during the execution of the sql.
 * Returns the result of the statement otherwise. See Statement.execute for more information.
 */
fun Connection.execute(sql:String, parameters: Map<String, Any?>): Boolean? {
    return execute(insertMapValues(sql, parameters))
}

/**
 * Executes the given sql string. Will return null if a Statement was not created or an exception occurred during the execution of the sql.
 * Returns the result of the statement otherwise. See Statement.execute for more information.
 */
fun Connection.execute(sql:String): Boolean? {
    var stmt: Statement? = null

    try {
        stmt = this.createStatement() ?: return null
        return stmt.execute(sql)
    } catch (e: Exception) {
        // Log something? Throw error?
    } finally {
        stmt?.close()
    }

    return null
}

private fun insertMapValues(sql: String, parameters: Map<String, Any?>?): String {
    var localSql = sql

    if (parameters != null && parameters.count() > 0) {
        for ((key, value) in parameters) {
            val rgx = Regex("@$key", RegexOption.IGNORE_CASE)
            val sqlValue = if (shouldQuote(value!!::class.java)) "'$value'" else value.toString()

            localSql = localSql.replace(rgx, sqlValue)
        }
    }

    return localSql
}

private fun <T> executeTextCommandWithResults(conn: Connection, sql: String, clazz: Class<T>): List<T> {
    var stmt: Statement? = null
    var rs: ResultSet? = null

    try {
        stmt = conn.createStatement() ?: return emptyList()
        rs = stmt.executeQuery(sql) ?: return emptyList()

        val results = Vector<T>()

        while (rs.next()) {
            val m = mapModel(rs, clazz) ?: continue

            results.add(m)
        }

        return results
    } catch (e: Exception) {
        // Log something? Throw error?
    } finally {
        stmt?.close()
        rs?.close()
    }

    return emptyList()
}

private fun <T> executeStoredProcWithResults(conn: Connection, sql: String, parameters: Map<String, Any?>, clazz: Class<T>): List<T> {
    var stmt: CallableStatement? = null
    var rs: ResultSet? = null

    try {
        val escapedSql = jdbcEscape(sql, parameters)

        stmt = conn.prepareCall(escapedSql) ?: return emptyList()

        if (parameters.count() > 0) {
            for ((key, value) in parameters)
                stmt.setObject(key, value)
        }

        stmt.execute()

        rs = stmt.resultSet ?: return emptyList()

        val results = Vector<T>()

        while (rs.next()) {
            val m = mapModel(rs, clazz) ?: continue

            results.add(m)
        }

        return results
    } catch (e: SQLException) {
        println(e.message)
        // Log something? Throw something?
    } finally {
        stmt?.close()
        rs?.close()
    }

    return emptyList()
}

/// TODO: I don't like this, but I can't seem to make the types work properly to call into the Class<T> version
private fun executeTextCommandWithResults(conn: Connection, sql: String): List<Map<String, Any?>> {
    var stmt: Statement? = null
    var rs: ResultSet? = null

    try {
        stmt = conn.createStatement() ?: return emptyList()
        rs = stmt.executeQuery(sql) ?: return emptyList()

        val metadata = rs.metaData
        val columnNames = (1..metadata.columnCount).map { metadata.getColumnName(it) }

        val results = Vector<Map<String, Any?>>()

        while (rs.next()) {
            val currMap = HashMap<String, Any?>()

            for (name in columnNames)
                currMap.put(name, rs.getObject(name))

            results.add(currMap)
        }

        return results
    } catch (e: Exception) {

    } finally {
        stmt?.close()
        rs?.close()
    }

    return emptyList()
}

private fun executeStoredProcWithResults(conn: Connection, sql: String, parameters: Map<String, Any?>): List<Map<String, Any?>> {
    var stmt: CallableStatement? = null
    var rs: ResultSet? = null

    try {
        val escapedSql = jdbcEscape(sql, parameters)

        stmt = conn.prepareCall(escapedSql) ?: return emptyList()

        if (parameters.count() > 0) {
            for ((key, value) in parameters)
                stmt.setObject(key, value)
        }

        stmt.execute()

        rs = stmt.resultSet ?: return emptyList()

        val metadata = rs.metaData
        val columnNames = (1..metadata.columnCount).map { metadata.getColumnName(it) }

        val results = Vector<Map<String, Any?>>()

        while (rs.next()) {
            val currMap = HashMap<String, Any?>()

            for (name in columnNames)
                currMap.put(name, rs.getObject(name))

            results.add(currMap)
        }

        return results
    } catch (e: SQLException) {
        println(e.message)
        // Log something? Throw something?
    } finally {
        stmt?.close()
        rs?.close()
    }

    return emptyList()
}