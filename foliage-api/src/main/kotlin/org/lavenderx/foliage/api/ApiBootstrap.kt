package org.lavenderx.foliage.api

import org.lavenderx.foliage.nettyrpc.logging.loggerFor
import org.lavenderx.foliage.api.web.WebServer

object ApiBootstrap {
    private val SERVER_PORT = "foliage.server.port"
    private val logger = loggerFor<ApiBootstrap>()

    fun run() {
        val port = if (System.getProperty(SERVER_PORT) == null) 8080 else System.getProperty(SERVER_PORT).toInt()
        val webServer = WebServer("FoliageServer", port).start()

        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                webServer.destroy()
            } catch (e: Exception) {
                logger.error("An exception occurred when destroying server", e)
            }
        })
    }
}

fun main(args: Array<String>) {
    ApiBootstrap.run()
}