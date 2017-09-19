package com.pinchotsoft.spiffy

import com.pinchotsoft.spiffy.models.Card
import com.pinchotsoft.spiffy.models.Card2
import com.pinchotsoft.spiffy.sqlserver.SqlServerDriverProvider
import org.junit.Before
import org.junit.Test

class QueryTests {
    private val connString = "jdbc:sqlserver://localhost;instanceName=sqlexpress;databaseName=Core;integratedSecurity=false;"
    private val user = "s.mctesterson"
    private val pass = "mib20!!"

    private lateinit var connectionFactory: ConnectionFactory

    @Before
    fun setUp() {
        connectionFactory = ConnectionFactory(SqlServerDriverProvider())
    }

    @Test
    fun test_simpleSql_handlesPrimitive() {
        connectionFactory.get(connString, user, pass).use {
            val results = it.query("select cost from cards where Id = 1", Int::class.java)

            assert(results.count() > 0)
            assert(results.first() > 0)
        }
    }

    @Test
    fun test_simpleSql_handlesDataClass() {
        connectionFactory.get(connString, user, pass).use {
            val results = it.query("select * from Cards", Card::class.java)

            assert(results.count() > 1)
            assert(results.first().name != "")
        }
    }

    @Test
    fun test_simpleSql_handlesPojo() {
        connectionFactory.get(connString, user, pass).use {
            val results = it.query("select * from cards", Card2::class.java)

            assert(results.count() > 1)
            assert(results.first().name != "")
        }
    }

    @Test
    fun test_query_supportsMap() {
        connectionFactory.get(connString, user, pass).use {
            val result = it.query("select * from cards where Id = @ID", mapOf("id" to 1), Card::class.java).firstOrNull()

            assert(result != null)

            assert(result!!.name != "")
        }
    }

    @Test
    fun test_query_supportsTemplate() {
        connectionFactory.get(connString, user, pass).use {
            val template = Card(2, "", null, 1, 2)

            val result = it.query("select * from cards where Id = @id", template).firstOrNull()

            assert(result != null)

            assert(result!!.name != "")
        }
    }
}