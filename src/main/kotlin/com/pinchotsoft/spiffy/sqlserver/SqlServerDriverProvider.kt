package com.pinchotsoft.spiffy.sqlserver

import com.microsoft.sqlserver.jdbc.SQLServerDriver
import com.pinchotsoft.spiffy.DbDriverProvider
import java.sql.Driver

class SqlServerDriverProvider : DbDriverProvider {
    private var registered = false

    override val isRegistered: Boolean
        get() = registered

    override fun getDriver(): Driver {
        registered = true
        return SQLServerDriver()
    }
}