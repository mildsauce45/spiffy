package com.pinchotsoft.spiffy.utilities

class GenericStringTransformer<out T : Any>(private val clazz: Class<T>) {
    companion object {
        inline operator fun <reified T : Any>invoke() = GenericStringTransformer(T::class.java)
    }

    fun transformToString(t: Any, transform: (Any) -> String): String {
        return when {
            clazz.isAssignableFrom(t.javaClass) -> transform(t)
            else -> t.toString()
        }
    }

    fun transform(t: Any, transform: (Any, Boolean) -> Pair<String, Map<String, Any?>>): Pair<String, Map<String, Any?>> {
        return when {
            clazz.isAssignableFrom(t.javaClass) -> transform(t, true)
            else -> transform(t, false)
        }
    }
}