package com.pinchotsoft.spiffy

import org.junit.Test

class SqlHelpersTest {

    @Test
    fun sqlHelpers_transformSql_noParams() {
        val query = "select * from orders where Id = 1"

        val (transformedSql, _) = transformSql(query, emptyMap())

        assert(query == transformedSql)
    }

    @Test
    fun sqlHelpers_transformSql_singleParam() {
        val (transformedSql, transformedInputs) = transformSql("select * from orders where Id = @id", mapOf("ID" to 1))

        assert(transformedSql == "select * from orders where Id = ?")
        assert(transformedInputs.count() == 1)
        assert(transformedInputs[1] == 1)
    }

    @Test
    fun sqlHelpers_transformSql_multipleParams() {
        val (transformedSql, transformedInputs) = transformSql("select * from orders where Id = @id and customerId = @customerId order by Id", mapOf("ID" to 1, "CustomerId" to 3))

        assert(transformedSql == "select * from orders where Id = ? and customerId = ? order by Id")
        assert(transformedInputs.count() == 2)
        assert(transformedInputs[1] == 1)
        assert(transformedInputs[2] == 3)
    }

    @Test
    fun sqlHelpers_transformSql_handlesIterables() {
        val initialQuery = "select * from orders where Id in @Ids"
        val desiredQuery = "select * from orders where Id in (?,?,?)"
        val inputs = mapOf("Ids" to listOf(1, 2, 3))

        val (transformedSql, transformedInputs) = transformSql(initialQuery, inputs)

        assert(transformedSql == desiredQuery)
        assert(transformedInputs.count() == 3)
        assert(transformedInputs.containsValue(1))
        assert(transformedInputs.containsValue(2))
        assert(transformedInputs.containsValue(3))
    }
}