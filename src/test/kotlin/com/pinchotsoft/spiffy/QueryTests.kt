package com.pinchotsoft.spiffy

import com.pinchotsoft.spiffy.models.Order
import com.pinchotsoft.spiffy.models.OrderPojo
import org.junit.Test

class QueryTests {

    @Test
    fun test_simpleSql_handlesPrimitive() {
        TestHelpers.getConnection().use {
            val results = it.query("select unitsinstock from products where productid = 1", Int::class.java)

            assert(results.count() > 0)
            assert(results.first() > 0)
        }
    }

    @Test
    fun test_simpleSql_handlesDataClass() {
        TestHelpers.getConnection().use {
            val results = it.query("select * from Orders", Order::class.java)

            assert(results.count() > 1)
            assert(results.first().shipName != "")
        }
    }

    @Test
    fun test_simpleSql_handlesPojo() {
        TestHelpers.getConnection().use {
            val results = it.query("select * from Orders", OrderPojo::class.java)

            assert(results.count() > 1)
            assert(results.first().shipName != "")
        }
    }

    @Test
    fun test_query_supportsMap() {
        TestHelpers.getConnection().use {
            val result = it.query("select * from orders where orderid = @ID", mapOf("id" to 10248), Order::class.java).firstOrNull()

            assert(result != null)

            assert(result!!.shipName != "")
        }
    }

    @Test
    fun test_query_supportsTemplate() {
        TestHelpers.getConnection().use {
            val template = Order(10249, null, null, null, null, null, null, null, null, null, null, null, null, null)

            val result = it.query("select * from orders where orderId = @orderId", template).firstOrNull()

            assert(result != null)

            assert(result!!.shipName != "")
        }
    }

    @Test
    fun test_query_selectDataClassSubset() {
        TestHelpers.getConnection().use {

            val result = it.query("select shipName, freight, shipPostalCode from orders where orderId = 10249", Order::class.java).firstOrNull()
            assert(result != null)

            assert(result!!.orderId == 0) // because we didnt map that field in the select statement
            assert(result.shipVia == 0) // same reason
            assert(result.shipPostalCode != "")
        }
    }

    @Test
    fun test_query_selectPojoSubset() {
        TestHelpers.getConnection().use {
            val result = it.query("select shipName, freight, shipPostalCode from orders where orderId = 10249", OrderPojo::class.java).firstOrNull()

            assert(result != null)

            assert(result!!.orderId == 0) // because we didnt map that field in the select statement
            assert(result.shipVia == null) // same reason
            assert(result.shipPostalCode != "")
        }
    }

    @Test
    fun test_query_selectUntypedResults() {
        TestHelpers.getConnection().use {
            val result = it.query("select * from orders")

            assert(result.count() > 1)

            val order = result.first()

            assert(order.containsKey("OrderID"))
            assert(order.containsKey("CustomerID"))
            assert(order.containsKey("EmployeeID"))
            assert(order.containsKey("OrderDate"))
            assert(order.containsKey("Freight"))
        }
    }

    @Test
    fun test_query_filteringWithNull() {
        TestHelpers.getConnection().use {
            val result = it.query("select * from orders where shippeddate is @NULLPARAM", mapOf("nullparam" to null))

            assert(result.count() > 1)
        }
    }

    @Test
    fun test_query_selectWithIterable() {
        TestHelpers.getConnection().use {
            val result = it.query("select distinct EmployeeId from Orders where EmployeeId in @EmployeeIds", mapOf("employeeIds" to listOf(1, 2, 3)), Int::class.java)

            assert(result.count() == 3)
            assert(result.contains(1))
            assert(result.contains(2))
            assert(result.contains(3))
        }
    }
}