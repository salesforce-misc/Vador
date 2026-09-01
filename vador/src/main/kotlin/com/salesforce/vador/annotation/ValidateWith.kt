package com.salesforce.vador.annotation

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class ValidateWith(val validator: KClass<*>, val failureKey: String)
