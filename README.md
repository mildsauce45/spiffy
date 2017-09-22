# spiffy
A kotlin micro-ORM based on the dapper c# project

## Intro

Coming from a .NET fullstack background, I've come to rely heavily on Dapper (https://github.com/StackExchange/Dapper) for database access.
It's quick, easy to use, and allows for hand tuning of queries in a way that Entity Framework just doesn't. 
As a way to teach myself Kotlin and familiarize myself with the Java development stack, I decided to try my hand at making a similar micro-ORM in Kotlin.

## Features

* Works as extension methods off of the standard `java.sql.Connection` interface and will support any database with a driver you've registered
* Supports Kotlin data classes, POJOs, and primitives as result types
* Supports Stored procedure calls
* Supports parameterization of sql queries in a number of ways

## Benchmarks
Currently spiffy is reporting ~250ms for 500 selects in the Northwind DB against the Orders table, the bulk of which is taken up by the first call, as the caches of reflection information get primed. This is decent, but I'd like to get it down to roughly the performance of the sql2o project. After a little more tuning, I'll create a fancy table reporting more concrete stats. 

### Result Types

Primitives

```kotlin
connectionFactory.get().use {
    val results = it.query("select cost from cards where Id = 1", Int::class.java)
}
```

POJOs (Written in Kotlin)

```kotlin
class Card {
    var id: Int = 0
    var name: String = ""
    var text: String? = null
    var cardType: Int = 0
    var cost: Int = 0
}
```

Kotlin Data Classes

```kotlin
data class Card(var id: Int, var name: String, var text: String?, var cardType: Int, var cost: Int)
```

### Stored Produced Calls

Parameterless invocations

```kotlin
connectionFactory.get().use {
    val res = it.query("spNoInputParams", Card::class.java, CommandType.STORED_PROCEDURE)
}
```

With input parameters

```kotlin
connectionFactory.get().use {
    val res = it.query("spGetCardsForUser", mapOf("UserId" to 1), Card::class.java, CommandType.STORED_PROCEDURE)
}
```

### Parameterized Queries

For all parameterized calls the variable name must be proceeded by an `@`. The name of the variable is case-insensitive and
will match either the key in the map or the field on the provided template object.

Via maps

```kotlin
connectionFactory.get().use {
    val result = it.query("select * from cards where Id = @ID", mapOf("id" to 1), Card::class.java).firstOrNull()
}
```

Via templated objects

```kotlin
connectionFactory.get().use {
    val template = Card(2, "", null, 1, 2)

    val result = it.query("select * from cards where Id = @id", template).firstOrNull()
}
```
