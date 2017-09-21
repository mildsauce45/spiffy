package com.pinchotsoft.spiffy

import com.pinchotsoft.spiffy.sqlserver.SqlServerDriverProvider
import java.sql.Connection

class TestHelpers {
    companion object {
        private val CONN_STRING = "jdbc:sqlserver://localhost;instanceName=sqlexpress;databaseName=Core;integratedSecurity=false;"
        private val USER = "s.mctesterson"
        private val PASS = "mib20!!"

        private val NORTHWIND_CONN_STRING = "jdbc:sqlserver://localhost;instanceName=sqlexpress;databaseName=Northwind;integratedSecurity=false;"

        private var connectionFactory = ConnectionFactory(SqlServerDriverProvider())

        fun getConnection(): Connection {
            return connectionFactory.get(CONN_STRING, USER, PASS)
        }

        fun getNorthwindConnection(): Connection {
            return connectionFactory.get(NORTHWIND_CONN_STRING, USER, PASS)
        }
    }
}