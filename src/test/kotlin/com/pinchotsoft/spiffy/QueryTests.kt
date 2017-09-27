package com.pinchotsoft.spiffy

import com.pinchotsoft.spiffy.models.Card
import com.pinchotsoft.spiffy.models.Card2
import com.pinchotsoft.spiffy.models.Order
import com.pinchotsoft.spiffy.utilities.Stopwatch
import org.junit.Test

class QueryTests {

    @Test
    fun test_simpleSql_handlesPrimitive() {
        TestHelpers.getConnection().use {
            val results = it.query("select cost from cards where Id = 1", Int::class.java)

            assert(results.count() > 0)
            assert(results.first() > 0)
        }
    }

    @Test
    fun test_simpleSql_handlesDataClass() {
        TestHelpers.getConnection().use {
            val results = it.query("select * from Cards", Card::class.java)

            assert(results.count() > 1)
            assert(results.first().name != "")
        }
    }

    @Test
    fun test_simpleSql_handlesPojo() {
        TestHelpers.getConnection().use {
            val results = it.query("select * from cards", Card2::class.java)

            assert(results.count() > 1)
            assert(results.first().name != "")
        }
    }

    @Test
    fun test_query_supportsMap() {
        TestHelpers.getConnection().use {
            val result = it.query("select * from cards where Id = @ID", mapOf("id" to 1), Card::class.java).firstOrNull()

            assert(result != null)

            assert(result!!.name != "")
        }
    }

    @Test
    fun test_query_supportsTemplate() {
        TestHelpers.getConnection().use {
            val template = Card(2, "", null, 1, 2)

            val result = it.query("select * from cards where Id = @id", template).firstOrNull()

            assert(result != null)

            assert(result!!.name != "")
        }
    }

    @Test
    fun test_query_selectDataClassSubset() {
        TestHelpers.getConnection().use {
            val result = it.query("select name, text, cardtype from cards where Id = 1", Card::class.java).firstOrNull()

            assert(result != null)

            assert(result!!.id == 0) // because we didnt map that field in the select statement
            assert(result.cost == 0) // same reason
            assert(result.name != "")
        }
    }

    @Test
    fun test_query_selectPojoSubset() {
        TestHelpers.getConnection().use {
            val result = it.query("select name, text, cardtype from cards where Id = 1", Card2::class.java).firstOrNull()

            assert(result != null)

            assert(result!!.id == 0) // because we didnt map that field in the select statement
            assert(result.cost == 0) // same reason
            assert(result.name != "")
        }
    }

    @Test
    fun test_query_selectUntypedResults() {
        TestHelpers.getConnection().use {
            val result = it.query("select * from cards")

            assert(result.count() > 1)

            val card = result.first()

            assert(card.containsKey("Id"))
            assert(card.containsKey("Name"))
            assert(card.containsKey("Text"))
            assert(card.containsKey("Cost"))
            assert(card.containsKey("CardType"))
        }
    }

    @Test
    fun test_query_filtingWithNull() {
        TestHelpers.getNorthwindConnection().use {
            val result = it.query("select * from orders where shippeddate is @NULLPARAM", mapOf("nullparam" to null))

            assert(result.count() > 1)
        }
    }

    @Test
    fun test_query_selectWithIterable() {
        TestHelpers.getNorthwindConnection().use {
            val result = it.query("select distinct EmployeeId from Orders where EmployeeId in @EmployeeIds", mapOf("employeeIds" to listOf(1, 2, 3)), Int::class.java)

            assert(result.count() == 3)
            assert(result.contains(1))
            assert(result.contains(2))
            assert(result.contains(3))
        }
    }
}