package com.pinchotsoft.spiffy

import com.pinchotsoft.spiffy.models.OrderHistory
import org.junit.Test

class SprocTests {
    @Test
    fun test_sproc_withQueryOnly() {
        TestHelpers.getConnection().use {
           val res = it.query("[Ten Most Expensive Products]", String::class.java, CommandType.STORED_PROCEDURE)

           assert(res.count() == 10)
           assert(res.first() != "")
       }
    }

    @Test
    fun test_sproc_withInputParams() {
        TestHelpers.getConnection().use {
            val res = it.query("[dbo].[CustOrderHist]", mapOf("CustomerID" to "CENTC"), OrderHistory::class.java, CommandType.STORED_PROCEDURE)

            assert(res.count() > 0)
            assert(res.first().productName == "Sir Rodney's Scones")
            assert(res.first().total == 10)
        }
    }

    @Test
    fun test_sproc_withUntypedResults() {
        TestHelpers.getConnection().use {
            val res = it.query("CustOrdersDetail", mapOf("OrderId" to 10268), commandType = CommandType.STORED_PROCEDURE)

            assert(res.count() > 1)

            val card = res.first()

            assert(card.containsKey("ProductName"))
            assert(card.containsKey("UnitPrice"))
            assert(card.containsKey("Quantity"))
            assert(card.containsKey("Discount"))
            assert(card.containsKey("ExtendedPrice"))
        }
    }
}