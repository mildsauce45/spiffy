package com.pinchotsoft.spiffy

import java.sql.Driver

interface DbDriverProvider {
    val isRegistered: Boolean

    fun getDriver(): Driver
}