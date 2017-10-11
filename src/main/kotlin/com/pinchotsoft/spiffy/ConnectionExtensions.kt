package com.pinchotsoft.spiffy

import com.pinchotsoft.spiffy.mapping.ResultContext
import com.pinchotsoft.spiffy.mapping.mapModel
import java.sql.*
import java.util.Vector

/**
 * Executes the given sql, after substituting the values in the map. The replacement is case-insensitive.
 * Returns a list of the given type from the result set.
 */
fun <T> Connection.query(sql: String, parameters: Map<String, Any?>, clazz: Class<T>, commandType: CommandType = CommandType.TEXT): List<T> {
    return if (commandType == CommandType.TEXT)
        executeTextCommandWithResults(this, sql, clazz, parameters)
    else
        executeStoredProcWithResults(this, sql, parameters, clazz)
}

/**
 * Executes the given sql, substituting any matching values found on the given template object. The name matching is case-insensitive.
 * Returns a list of the given type from the result set.
 */
fun <T> Connection.query(sql: String, template: T, commandType: CommandType = CommandType.TEXT): List<T> where T : kotlin.Any {
    val clazz = template.javaClass
    val fields = ReflectionHelper.getFieldsForClass(clazz)
    val inputMap = HashMap<String, Any?>()

    for (f in fields)
        inputMap.put(f.name, getFieldValue(template, f, clazz))

    return query(sql, inputMap, clazz, commandType)
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

/**
 * Executes the given sql and returns a list of Map<String, Any?>
 */
fun Connection.query(sql: String, parameters: Map<String, Any?>? = null, commandType: CommandType = CommandType.TEXT): List<Map<String, Any?>> {
    return if (commandType == CommandType.TEXT)
        executeTextCommandWithResults(this, sql, parameters)
    else
        executeStoredProcWithResults(this, sql, parameters ?: emptyMap())
}

/**
 * Executes the given sql after substituting the values in the map. The replacement is case-insensitive.
 * Will return null if a Statement was not created or an exception occurred during the execution of the sql.
 * Returns the result of the statement otherwise. See Statement.execute for more information.
 */
fun Connection.execute(sql: String, parameters: Map<String, Any?>? = null): Boolean? {
    var stmt: PreparedStatement? = null

    try {
        val (transformedSql, transformedInputs) = transformSql(sql, parameters ?: emptyMap())

        stmt = this.prepareStatement(transformedSql) ?: return null

        if (transformedInputs.count() > 0) {
            for ((key, value) in transformedInputs)
                stmt.setObject(key, value)
        }

        return stmt.execute()
    } catch (e: Exception) {
        // Log something? Throw error?
    } finally {
        stmt?.close()
    }

    return null
}

private fun <T> executeTextCommandWithResults(conn: Connection, sql: String, clazz: Class<T>, parameters: Map<String, Any?>? = null): List<T> {
    val results = Vector<T>()
    val parms = parameters ?: emptyMap()

    executeTextCommand(conn, sql, parms) {
        val context = ResultContext(it, clazz)

        while (it.next()) {
            val m = mapModel(it, context, clazz) //?: continue

            results.add(m)
        }
    }


    return results
}

private fun <T> executeStoredProcWithResults(conn: Connection, sql: String, parameters: Map<String, Any?>, clazz: Class<T>): List<T> {
    val results = Vector<T>()

    executeStoredProcCommand(conn, sql, parameters) {
        val context = ResultContext(it, clazz)

        while (it.next()) {
            val m = mapModel(it, context, clazz) ?: continue

            results.add(m)
        }
    }

    return results
}

private fun executeTextCommandWithResults(conn: Connection, sql: String, parameters: Map<String, Any?>? = null): List<Map<String, Any?>> {
    val results = Vector<Map<String, Any?>>()
    val parms = parameters ?: emptyMap()

    executeTextCommand(conn, sql, parms) {
        val metadata = it.metaData
        val columnNames = (1..metadata.columnCount).map { metadata.getColumnName(it) }

        while (it.next()) {
            val currMap = HashMap<String, Any?>()

            for (name in columnNames)
                currMap.put(name, it.getObject(name))

            results.add(currMap)
        }
    }

    return results
}

private fun executeStoredProcWithResults(conn: Connection, sql: String, parameters: Map<String, Any?>): List<Map<String, Any?>> {
    val results = Vector<Map<String, Any?>>()

    executeStoredProcCommand(conn, sql, parameters) {
        val metadata = it.metaData
        val columnNames = (1..metadata.columnCount).map { metadata.getColumnName(it) }

        while (it.next()) {
            val currMap = HashMap<String, Any?>()

            for (name in columnNames)
                currMap.put(name, it.getObject(name))

            results.add(currMap)
        }
    }

    return results
}

private fun executeTextCommand(conn: Connection, sql: String, parameters: Map<String, Any?>, mapper: (ResultSet) -> Unit) {
    var stmt: PreparedStatement? = null
    var rs: ResultSet? = null

    try {
        val (transformedSql, transformedInputs) = transformSql(sql, parameters)

        stmt = conn.prepareStatement(transformedSql) ?: return

        if (transformedInputs.count() > 0) {
            for ((key, value) in transformedInputs)
                stmt.setObject(key, value)
        }

        rs = stmt.executeQuery() ?: return

        mapper(rs)
    } catch (e: Exception) {
        // Log something? Throw error?
    } finally {
        stmt?.close()
        rs?.close()
    }
}

private fun executeStoredProcCommand(conn: Connection, sql: String, parameters: Map<String, Any?>, mapper: (ResultSet) -> Unit) {
    var stmt: CallableStatement? = null
    var rs: ResultSet? = null

    try {
        val escapedSql = jdbcEscape(sql, parameters)

        stmt = conn.prepareCall(escapedSql) ?: return

        if (parameters.count() > 0) {
            for ((key, value) in parameters)
                stmt.setObject(key, value)
        }

        stmt.execute()

        rs = stmt.resultSet ?: return

        mapper(rs)
    } catch (e: Exception) {
        // Log something? Throw error?
    } finally {
        stmt?.close()
        rs?.close()
    }

}
