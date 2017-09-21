package com.pinchotsoft.spiffy.models

import java.math.BigDecimal
import java.sql.Timestamp

data class Order(
        var orderId: Int,
        var customerID: String?,
        var employeeId: Int?,
        var orderDate: Timestamp?,
        var requiredDate: Timestamp?,
        var shippedDate: Timestamp?,
        var shipVia: Int?,
        var shipName: String?,
        var shipAddress: String?,
        var shipCity: String?,
        var shipRegion: String?,
        var shipPostalCode: String?,
        var shipCountry: String?,
        var freight: BigDecimal?)