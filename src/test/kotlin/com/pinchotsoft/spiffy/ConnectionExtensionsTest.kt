package com.pinchotsoft.spiffy

import com.pinchotsoft.spiffy.models.Card
import com.pinchotsoft.spiffy.sqlserver.SqlServerDriverProvider
import org.junit.Before
import org.junit.Test
import kotlin.test.assert

class ConnectionExtensionsTest {
    val connString = "jdbc:sqlserver://localhost;instanceName=sqlexpress;databaseName=Core;integratedSecurity=false;"
    val user = "s.mctesterson"
    val pass = "mib20!!"

    lateinit var connectionFactory: ConnectionFactory

    @Before
    fun setUp() {
        connectionFactory = ConnectionFactory(SqlServerDriverProvider())
    }

    @Test
    fun test_simpleSql() {
        connectionFactory.get(connString, user, pass).use {
            val results = it.query("select * from Cards", Card::class.java)

            assert(results.count() > 0)
        }
    }
}