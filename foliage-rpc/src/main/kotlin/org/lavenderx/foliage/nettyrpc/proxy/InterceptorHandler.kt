package org.lavenderx.foliage.nettyrpc.proxy

import java.lang.reflect.Method

interface InterceptorHandler {

    @Throws(Throwable::class)
    fun invoke(proxy: Any, method: Method, vararg args: Any): Any
}