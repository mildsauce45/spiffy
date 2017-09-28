package com.pinchotsoft.spiffy

import java.sql.Driver

interface DbDriverProvider {
    fun getDriver(): Driver
}