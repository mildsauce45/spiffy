package com.pinchotsoft.spiffy

import java.sql.Connection
import java.sql.DriverManager

class TestHelpers {
    companion object {
        private val USER = "spiffy.test.user"
        private val PASS = "TestPassword01!!"

        private val NORTHWIND_CONN_STRING = "jdbc:sqlserver://localhost;databaseName=Northwind;integratedSecurity=false;"

        fun getConnection(): Connection {
            return DriverManager.getConnection(NORTHWIND_CONN_STRING, USER, PASS)
        }
    }
}