package com.pinchotsoft.spiffy.models

import java.sql.Timestamp

data class User(var id: Int, var userName: String, var createdDate: Timestamp, var firstName: String, var lastName: String)