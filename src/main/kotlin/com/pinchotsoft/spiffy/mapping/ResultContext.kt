package com.pinchotsoft.spiffy.mapping

import com.pinchotsoft.spiffy.ReflectionHelper
import com.pinchotsoft.spiffy.isPrimitive
import java.lang.reflect.Constructor
import java.sql.ResultSet

class ResultContext(rs: ResultSet, clazz: Class<*>) {
    private val columns: List<String>
    private val calculatedColumnAvailability = HashMap<String, Boolean>()

    val isPrimitiveType: Boolean
    val constructor: Constructor<*>
    val isDataClass: Boolean

    init {
        val metadata = rs.metaData

        columns = (1..metadata.columnCount).map { metadata.getColumnName(it) }

        isPrimitiveType = isPrimitive(clazz)

        val cons = ReflectionHelper.getClassConstructor(clazz, true)
        if (cons != null) {
            constructor = cons
            isDataClass = false
        } else {
            constructor = ReflectionHelper.getClassConstructor(clazz, false)!!
            isDataClass = true
        }
    }

    fun hasColumn(columnName: String): Boolean {
        if (!calculatedColumnAvailability.containsKey(columnName))
            calculatedColumnAvailability.put(columnName, columns.any { it.equals(columnName, true) })

        return calculatedColumnAvailability[columnName]!!
    }
}