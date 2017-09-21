package com.pinchotsoft.spiffy

import com.pinchotsoft.spiffy.models.User
import org.junit.Test

class ExecuteTests {

    @Test
    fun test_execute_performInsert() {
        TestHelpers.getConnection().use {
            it.execute("truncate table TestInsertTable")

            var count = it.query("select count(*) from TestInsertTable", clazz = Int::class.java).firstOrNull()

            assert(count != null && count == 0)

            it.execute("insert into TestInsertTable (Name, Description) values (@Name, @Desc)", mapOf("name" to "Foo", "desc" to "Bar"))

            count = it.query("select count(*) from TestInsertTable", clazz = Int::class.java).firstOrNull()

            assert(count != null && count == 1)
        }
    }

    @Test
    fun test_execute_performUpdate() {
        TestHelpers.getConnection().use {
            val fetchSql = "select * from users where username = 'jdarkmagic'"
            val updateSql = "update users set firstName = @FIRSTNAME"

            val expectedName = "Jim"
            val testUpdateName = "Gabe"

            val userToModify = it.query(fetchSql, User::class.java).firstOrNull()

            assert(userToModify != null)
            assert(userToModify!!.firstName == expectedName)

            it.execute(updateSql, mapOf("firstName" to testUpdateName))

            val modifiedUser = it.query(fetchSql, User::class.java).firstOrNull()

            assert(modifiedUser != null)
            assert(modifiedUser!!.firstName == testUpdateName)

            it.execute(updateSql, mapOf("firstName" to expectedName))
        }
    }
}