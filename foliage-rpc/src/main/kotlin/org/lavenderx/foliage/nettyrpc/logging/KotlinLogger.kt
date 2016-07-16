package org.lavenderx.foliage.nettyrpc.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

// A couple of inlined utility functions: the first is just a syntax convenience, the second lets us use
// Kotlin's string interpolation efficiently: the message is never calculated/concatenated together unless
// logging at that level is enabled.
inline fun <reified T : Any> loggerFor(): Logger = LoggerFactory.getLogger(T::class.java)
