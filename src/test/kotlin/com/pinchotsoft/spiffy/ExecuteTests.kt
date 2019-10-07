package com.pinchotsoft.spiffy

import com.pinchotsoft.spiffy.models.Product
import org.junit.Test

class ExecuteTests {

    @Test
    fun test_execute_performInsert() {
        TestHelpers.getConnection().use {
            it.execute("truncate table Promotions")

            var count = it.query("select count(*) from Promotions", clazz = Int::class.java).firstOrNull()

            assert(count != null && count == 0)

            it.execute("insert into Promotions (ProductId, Name, Description) values (@ProductId, @Name, @Desc)", mapOf("productId" to 1, "name" to "Foo", "desc" to "Bar"))

            count = it.query("select count(*) from TestInsertTable", clazz = Int::class.java).firstOrNull()

            assert(count != null && count == 1)
        }
    }

    @Test
    fun test_execute_performUpdate() {
        TestHelpers.getConnection().use {
            val fetchSql = "select * from products where productId = 11"
            val updateSql = "update products set productName = @PRODUCTNAME"

            val expectedName = "Queso Cabrales"
            val testUpdateName = "Cheese Sauce"

            val productToModify = it.query(fetchSql, Product::class.java).firstOrNull()

            assert(productToModify != null)
            assert(productToModify!!.productName == expectedName)

            it.execute(updateSql, mapOf("productName" to testUpdateName))

            val modifiedProduct = it.query(fetchSql, Product::class.java).firstOrNull()

            assert(modifiedProduct != null)
            assert(modifiedProduct!!.productName == testUpdateName)

            it.execute(updateSql, mapOf("firstName" to expectedName))
        }
    }

    @Test
    fun test_execute_performInsertWithNull() {
        TestHelpers.getConnection().use {
            val countSql = "select count(*) from Promotions"

            val initialCount = it.query(countSql, clazz = Int::class.java).firstOrNull()

            assert(initialCount != null)

            it.execute("insert into Promotions(ProductId, Name, Description) values (@productId, @name, @desc)", mapOf("productId" to 2, "name" to "Bar", "desc" to null ))

            val finalCount = it.query(countSql, clazz = Int::class.java).firstOrNull()

            assert(finalCount != null && finalCount > initialCount!!)
        }
    }
}