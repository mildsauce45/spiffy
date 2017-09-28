package com.pinchotsoft.spiffy.sqlserver

import com.microsoft.sqlserver.jdbc.SQLServerDriver
import com.pinchotsoft.spiffy.DbDriverProvider
import java.sql.Driver

class SqlServerDriverProvider : DbDriverProvider {
    override fun getDriver(): Driver {
        return SQLServerDriver()
    }
}