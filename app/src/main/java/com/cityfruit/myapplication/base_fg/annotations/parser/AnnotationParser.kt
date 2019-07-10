package com.cityfruit.myapplication.base_fg.annotations.parser

import java.lang.reflect.Field
import java.lang.reflect.Method

@Suppress("unused")
object AnnotationParser {

    inline fun <reified T : Annotation> parseMethod(clz: Class<*>): List<Pair<Method, T>> {
        val annotationSet = arrayListOf<Pair<Method, T>>()
        val tCls = T::class.java
        try {
            for (method in clz.declaredMethods) {
                val methodHasAno = method.isAnnotationPresent(tCls)
                if (methodHasAno) {
                    method.isAccessible = true
                    val pan = method.getAnnotation(tCls)
                    if (pan != null) {
                        annotationSet.add(Pair(method, pan))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return annotationSet
    }

    inline fun <reified T : Annotation> parseCls(clz: Class<*>): T? {
        val tCls = T::class.java
        val clzHasAno = clz.isAnnotationPresent(tCls)
        if (clzHasAno) {
            val annotation = clz.getAnnotation(tCls)
            if (annotation != null)
                return annotation
        }
        return null
    }

    inline fun <reified T : Annotation> parseField(clz: Class<*>): List<Pair<Field, T>> {
        val annotationSet = arrayListOf<Pair<Field, T>>()
        val tCls = T::class.java
        val fields = clz.declaredFields
        for (field in fields) {
            field.isAccessible = true
            val fieldHasAno = field.isAnnotationPresent(tCls)
            if (fieldHasAno) {
                val fieldAnnotation = field.getAnnotation(tCls)
                if (fieldAnnotation != null)
                    annotationSet.add(Pair(field, fieldAnnotation))
            }
        }
        return annotationSet
    }
}
