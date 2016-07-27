package io.foliage.netty.rpc.client

interface AsyncRpcCallback {

    fun success(result: Any)

    fun fail(e: Exception)
}