package io.foliage.netty.rpc.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class RpcListenerContainer(val value: KClass<*>)