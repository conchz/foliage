package org.lavenderx.foliage.api;

import lombok.extern.slf4j.Slf4j;
import org.lavenderx.foliage.api.web.WebServer;

import javax.servlet.ServletException;
import java.io.IOException;

@Slf4j
public class ApiBootstrap {

    private static final String SERVER_PORT = "foliage.server.port";

    public static void main(String[] args) {
        try {
            int port;
            if (System.getProperty(SERVER_PORT) == null) {
                port = 8080;
            } else {
                port = Integer.parseInt(System.getProperty(SERVER_PORT));
            }

            WebServer webServer = new WebServer("FoliageServer", port).start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    webServer.destroy();
                } catch (final Exception ex) {
                    log.error("An exception occurred when destroying server", ex);
                }
            }));
        } catch (IOException | ServletException ex) {
            log.error("", ex);
        }
    }
}
