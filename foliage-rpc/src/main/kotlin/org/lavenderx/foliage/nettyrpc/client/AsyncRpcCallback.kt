package org.lavenderx.foliage.nettyrpc.client

interface AsyncRpcCallback {

    fun success(result: Any)

    fun fail(e: Exception)
}