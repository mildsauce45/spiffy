package com.pinchotsoft.spiffy

enum class CommandType {
    /**
     * A sql statement (Default)
     */
    TEXT,

    /**
     * Calling a stored procedure
     */
    STORED_PROCEDURE
}