package com.pinchotsoft.spiffy.models

data class Card(var id: Int, var name: String, var text: String?, var cardType: Int, var cost: Int)

class Card2 {
    var id: Int = 0
    var name: String = ""
    var text: String? = null
    var cardType: Int = 0
    var cost: Int = 0
}