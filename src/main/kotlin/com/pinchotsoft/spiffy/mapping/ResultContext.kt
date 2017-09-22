package com.pinchotsoft.spiffy.mapping

import com.pinchotsoft.spiffy.ReflectionHelper
import com.pinchotsoft.spiffy.isPrimitive
import java.lang.reflect.Constructor
import java.sql.ResultSet

class ResultContext(rs: ResultSet, clazz: Class<*>) {
    private val columns: List<String>
    private val calculatedColumnAvailability = HashMap<String, Boolean>()
    private var _constructor: Constructor<*>? = null
    private var _isDataClass = false

    val isPrimitiveType: Boolean

    val isDataClass: Boolean
        get() = _isDataClass

    val constructor: Constructor<*>?
        get() = _constructor


    init {
        val metadata = rs.metaData

        columns = (1..metadata.columnCount).map { metadata.getColumnName(it) }

        isPrimitiveType = isPrimitive(clazz)

        if (!isPrimitiveType) {
            val cons = ReflectionHelper.getClassConstructor(clazz, true)
            if (cons != null) {
                _constructor = cons
            } else {
                _constructor = ReflectionHelper.getClassConstructor(clazz, false)!!
                _isDataClass = true
            }
        }
    }

    fun hasColumn(columnName: String): Boolean {
        if (!calculatedColumnAvailability.containsKey(columnName))
            calculatedColumnAvailability.put(columnName, columns.any { it.equals(columnName, true) })

        return calculatedColumnAvailability[columnName]!!
    }
}