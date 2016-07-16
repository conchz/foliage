package org.lavenderx.foliage.nettyrpc.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
annotation class RpcListenerContainer(val value: KClass<*>)