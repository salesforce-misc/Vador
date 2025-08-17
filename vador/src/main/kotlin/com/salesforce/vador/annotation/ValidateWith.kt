package com.salesforce.vador.annotation

import kotlin.reflect.KClass

@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@kotlin.annotation.Target(AnnotationTarget.FIELD)
annotation class ValidateWith(val validator: KClass<*>, val failureKey: String)
