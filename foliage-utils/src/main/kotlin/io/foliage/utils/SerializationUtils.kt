package io.foliage.utils

import io.protostuff.LinkedBuffer
import io.protostuff.ProtostuffIOUtil
import io.protostuff.Schema
import io.protostuff.runtime.RuntimeSchema
import org.objenesis.ObjenesisStd
import java.util.concurrent.ConcurrentHashMap

object SerializationUtils {

    private val cachedSchema = ConcurrentHashMap<Class<*>, Schema<*>>()
    private val objenesis = org.objenesis.ObjenesisStd(true)

    @Suppress("UNCHECKED_CAST")
    private fun <T> getSchema(clazz: Class<T>): io.protostuff.Schema<T> {
        var schema: io.protostuff.Schema<T>? = cachedSchema[clazz] as io.protostuff.Schema<T>
        if (schema == null) {
            schema = io.protostuff.runtime.RuntimeSchema.createFrom(clazz)
            if (schema != null) {
                cachedSchema.put(clazz, schema)
            }
        }
        return schema as io.protostuff.Schema<T>
    }

    fun <T : Any> serialize(obj: T): ByteArray {
        val buffer = io.protostuff.LinkedBuffer.allocate(io.protostuff.LinkedBuffer.DEFAULT_BUFFER_SIZE)
        try {
            val schema = getSchema(obj.javaClass)
            return io.protostuff.ProtostuffIOUtil.toByteArray(obj, schema, buffer)
        } catch (e: Exception) {
            throw IllegalStateException(e.message, e)
        } finally {
            buffer.clear()
        }
    }

    fun <T> deserialize(data: ByteArray, clazz: Class<T>): T {
        try {
            val message = objenesis.newInstance(clazz)
            val schema = getSchema(clazz)
            io.protostuff.ProtostuffIOUtil.mergeFrom(data, message, schema)
            return message
        } catch (e: Exception) {
            throw IllegalStateException(e.message, e)
        }

    }
}