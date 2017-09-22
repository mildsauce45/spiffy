package com.pinchotsoft.spiffy

import com.pinchotsoft.spiffy.models.Order
import com.pinchotsoft.spiffy.models.OrderPojo
import com.pinchotsoft.spiffy.utilities.Stopwatch
import org.junit.Test

class PerformanceTests {

    @Test
    fun test_query_benchmarks_dc() {
        TestHelpers.getNorthwindConnection().use {
            val clazz = Order::class.java

            val totalTime = Stopwatch.elapse {
                (1..501).forEach { i -> it.query("select * from orders where Id = @Id", mapOf("id" to i), clazz) }
            }

            assert(totalTime < 350)
        }
    }

    @Test
    fun test_query_pojo() {
        TestHelpers.getNorthwindConnection().use {
            val clazz = OrderPojo::class.java

            val totalTime = Stopwatch.elapse {
                (1..501).forEach { i -> it.query("select * from orders where Id = @Id", mapOf("id" to i), clazz) }
            }

            assert(totalTime < 350)
        }
    }

    @Test
    fun test_query_template() {
        TestHelpers.getNorthwindConnection().use {
            val totalTime = Stopwatch.elapse {
                (1..501).forEach { i ->
                    val template = Order(i, null, null, null, null, null, null, null, null, null, null, null, null, null)

                    it.query("select * from orders where Id = @Id", template)
                }
            }

            assert(totalTime < 350)
        }
    }

    @Test
    fun test_query_untypedMap() {
        TestHelpers.getNorthwindConnection().use {
            val totalTime = Stopwatch.elapse {
                (1..501).forEach { i ->
                    it.query("select * from orders where Id = @ID", mapOf("id" to i))
                }
            }

            assert(totalTime < 300)
        }
    }
}