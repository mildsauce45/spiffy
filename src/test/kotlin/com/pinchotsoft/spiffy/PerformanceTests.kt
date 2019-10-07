package com.pinchotsoft.spiffy

import com.pinchotsoft.spiffy.models.Order
import com.pinchotsoft.spiffy.models.OrderPojo
import com.pinchotsoft.spiffy.utilities.Stopwatch
import org.junit.Test

class PerformanceTests {

    @Test
    fun test_query_benchmarks_dc() {
        TestHelpers.getConnection().use {
            val clazz = Order::class.java

            val totalTime = Stopwatch.elapse {
                (10248..10749).forEach { i -> it.query("select * from orders where OrderId = @Id", mapOf("id" to i), clazz) }
            }

            assert(totalTime < 350)
        }
    }

    @Test
    fun test_query_pojo() {
        TestHelpers.getConnection().use {
            val clazz = OrderPojo::class.java

            val totalTime = Stopwatch.elapse {
                (10248..10749).forEach { i -> it.query("select * from orders where OrderId = @Id", mapOf("id" to i), clazz) }
            }

            assert(totalTime < 350)
        }
    }

    @Test
    fun test_query_template() {
        TestHelpers.getConnection().use {
            val totalTime = Stopwatch.elapse {
                (10248..10749).forEach { i ->
                    val template = Order(i, null, null, null, null, null, null, null, null, null, null, null, null, null)

                    it.query("select * from orders where OrderId = @OrderId", template)
                }
            }

            assert(totalTime < 350)
        }
    }

    @Test
    fun test_query_untypedMap() {
        TestHelpers.getConnection().use {
            val totalTime = Stopwatch.elapse {
                (10248..10749).forEach { i ->
                    it.query("select orderId, customerID, employeeId, orderDate, requiredDate, shippedDate, shipVia, shipName, shipAddress, shipCity, shipRegion, shipPostalCode, shipCountry, freight from orders where OrderId = @ID", mapOf("id" to i))
                }
            }

            assert(totalTime < 400)
        }
    }
}