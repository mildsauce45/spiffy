package com.pinchotsoft.spiffy

import com.pinchotsoft.spiffy.models.Card
import org.junit.Test

class SprocTests {
    @Test
    fun test_sproc_withQueryOnly() {
        TestHelpers.getConnection().use {
           val res = it.query("spNoInputParams", Card::class.java, CommandType.STORED_PROCEDURE)

           assert(res.count() > 0)
           assert(res.first().name != "")
       }
    }

    @Test
    fun test_sproc_withInputParams() {
        TestHelpers.getConnection().use {
            val res = it.query("spGetCardsForUser", mapOf("UserId" to 1), Card::class.java, CommandType.STORED_PROCEDURE)

            assert(res.count() > 0)
            assert(res.first().name != "")
        }
    }

    @Test
    fun test_sproc_withUntypedResults() {
        TestHelpers.getConnection().use {
            val res = it.query("spGetCardsForUser", mapOf("UserId" to 1), commandType = CommandType.STORED_PROCEDURE)

            assert(res.count() > 1)

            val card = res.first()

            assert(card.containsKey("Id"))
            assert(card.containsKey("Name"))
            assert(card.containsKey("Text"))
            assert(card.containsKey("Cost"))
            assert(card.containsKey("CardType"))

            // This is different than the typed results because our model doesn't throw it out
            assert(card.containsKey("Quantity"))
        }
    }
}