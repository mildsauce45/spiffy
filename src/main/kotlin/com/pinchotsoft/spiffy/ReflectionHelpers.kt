package com.pinchotsoft.spiffy

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

fun <T> getFieldValue(obj: T, field: Field, clazz: Class<T>): Any? {
    val getter = ReflectionHelper.getGetterForField(clazz, field.name) ?: return null

    return getter.invoke(obj)
}

fun isPrimitive(clazz: Class<*>): Boolean {
    return clazz.isPrimitive || ReflectionHelper.WRAPPER_TYPES.contains(clazz)
}

fun getDefaultValue(clazz: Class<*>): Any? {
    return ReflectionHelper.DEFAULT_VALUES[clazz]
}

internal class ReflectionHelper {
    companion object {
        val WRAPPER_TYPES: Set<Class<*>> = setOf(
                String::class.java,
                Boolean::class.java,
                Character::class.java,
                Byte::class.java,
                Short::class.java,
                Integer::class.java,
                Long::class.java,
                Float::class.java,
                Double::class.java,
                Void::class.java)

        val DEFAULT_VALUES: Map<Class<*>, Any?> = mapOf(
                String::class.java to "",
                Boolean::class.java to false,
                Character::class.java to ' ',
                Byte::class.java to 0,
                Short::class.java to 0,
                Integer::class.java to 0,
                Long::class.java to 0,
                Float::class.java to 0,
                Double::class.java to 0,
                Void::class.java to 0,
                Boolean::class.javaPrimitiveType!! to false,
                Char::class.javaPrimitiveType!! to ' ',
                Byte::class.javaPrimitiveType!! to 0,
                Short::class.javaPrimitiveType!! to 0,
                Int::class.javaPrimitiveType!! to 0,
                Long::class.javaPrimitiveType!! to 0,
                Float::class.javaPrimitiveType!! to 0,
                Double::class.javaPrimitiveType!! to 0)

        private val mappedFields: MutableMap<Class<*>, Array<Field>> = HashMap()
        private val fieldGetters: MutableMap<Pair<Class<*>, String>, Method> = HashMap()

        private val classMethods: MutableMap<Class<*>, Array<Method>> = HashMap()
        private val pojoConstructors: MutableMap<Class<*>, Constructor<*>?> = HashMap()
        private val dataClassConstructors: MutableMap<Class<*>, Constructor<*>?> = HashMap()

        fun getFieldsForClass(clazz: Class<*>): Array<Field> {
            if (!mappedFields.containsKey(clazz))
                mappedFields.put(clazz, clazz.declaredFields)

            return mappedFields[clazz]!!
        }

        fun getMethodsForClass(clazz: Class<*>): Array<Method> {
            if (!classMethods.containsKey(clazz))
                classMethods.put(clazz, clazz.methods)

            return classMethods[clazz]!!
        }

        fun getGetterForField(clazz: Class<*>, fieldName: String): Method? {
            val key = Pair(clazz, fieldName)
            if (!fieldGetters.containsKey(key)) {
                val method = getMethodsForClass(clazz).firstOrNull { it.name.equals("get$fieldName", true) } ?: return null
                fieldGetters.put(key, method)
            }

            return fieldGetters[key]!!
        }

        fun getClassConstructor(clazz: Class<*>, isPojo: Boolean): Constructor<*>? {
            return if (isPojo) {
                if (!pojoConstructors.containsKey(clazz))
                    pojoConstructors.put(clazz, clazz.constructors.firstOrNull { it.parameters.isEmpty() })

                pojoConstructors[clazz]
            } else {
                if (!dataClassConstructors.containsKey(clazz))
                    dataClassConstructors.put(clazz, clazz.constructors.first())

                dataClassConstructors[clazz]
            }
        }
    }
}

