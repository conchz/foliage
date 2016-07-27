package io.foliage.api

import io.foliage.api.web.WebServer
import io.foliage.utils.loggerFor

class ApiBootstrap {
    companion object {
        private val SERVER_PORT = "foliage.server.port"
        private val logger = loggerFor<ApiBootstrap>()

        @JvmStatic fun main(args: Array<String>) {
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
}