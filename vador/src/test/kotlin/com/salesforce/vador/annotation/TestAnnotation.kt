package com.salesforce.vador.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class TestAnnotation(val testParam: Int)
