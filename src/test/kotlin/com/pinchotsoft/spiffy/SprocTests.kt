package com.pinchotsoft.spiffy

import com.pinchotsoft.spiffy.models.Card
import com.pinchotsoft.spiffy.sqlserver.SqlServerDriverProvider
import org.junit.Before
import org.junit.Test

class SprocTests {
    private val connString = "jdbc:sqlserver://localhost;instanceName=sqlexpress;databaseName=Core;integratedSecurity=false;"
    private val user = "s.mctesterson"
    private val pass = "mib20!!"

    private lateinit var connectionFactory: ConnectionFactory

    @Before
    fun setUp() {
        connectionFactory = ConnectionFactory(SqlServerDriverProvider())
    }

    @Test
    fun test_sproc_withQueryOnly() {
       connectionFactory.get(connString, user, pass).use {
           val res = it.query("spNoInputParams", Card::class.java, CommandType.STORED_PROCEDURE)

           assert(res.count() > 0)
           assert(res.first().name != "")
       }
    }

    @Test
    fun test_sproc_withInputParams() {
        connectionFactory.get(connString, user, pass).use {
            val res = it.query("spGetCardsForUser", mapOf("UserId" to 1), Card::class.java, CommandType.STORED_PROCEDURE)

            assert(res.count() > 0)
            assert(res.first().name != "")
        }
    }
}