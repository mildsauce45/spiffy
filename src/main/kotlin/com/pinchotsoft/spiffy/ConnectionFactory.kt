package com.pinchotsoft.spiffy

import java.lang.IllegalStateException
import java.sql.Connection
import java.sql.DriverManager

class ConnectionFactory(private val driverProvider: DbDriverProvider) {
    companion object {
        val CONNECTION_STRING = "DB_CONNECTION_STRING"
        val USERNAME = "DB_USERNAME"
        val PASSWORD = "DB_PASSWORD"
    }

    private var isInitialized = false

    fun get(connString: String, user: String, pass: String): Connection {
        if (!isInitialized) {
            DriverManager.registerDriver(driverProvider.getDriver())
            isInitialized = true
        }

        return DriverManager.getConnection(connString, user, pass)
    }

    fun get(): Connection {
        val env = System.getenv()

        val connString = env[CONNECTION_STRING]
        val username = env[USERNAME]
        val password = env[PASSWORD]

        if (connString == null || username == null || password == null)
            throw IllegalStateException("Connection information not found in environment settings. Make sure to set $CONNECTION_STRING, $USERNAME, and $PASSWORD")

        return get(connString, username, password)
    }
}