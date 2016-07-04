package org.lavenderx.foliage.api.web;

import com.google.common.collect.Lists;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.handlers.DefaultServlet;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@Slf4j
public class WebServer implements DisposableBean {
    private final List<String> staticResourcesMappings =
            Lists.newArrayList("*.css", "*.js", "*.ico", "*.gif", "*.jpg", "*.jpeg", "*.png");

    private final String webAppName;
    private final int port;

    private Undertow undertowServer;
    private DeploymentManager deploymentManager;

    public WebServer(String webAppName, int port) {
        this.webAppName = webAppName;
        this.port = port;
    }

    public WebServer start() throws IOException, ServletException {
        InstanceFactory<WebServletContainerInitializer> instanceFactory =
                new ImmediateInstanceFactory<>(new WebServletContainerInitializer());
        ServletContainerInitializerInfo sciInfo = new ServletContainerInitializerInfo(
                WebServletContainerInitializer.class, instanceFactory, new HashSet<>()
        );

        ServletInfo defaultServlet = Servlets.servlet("default", DefaultServlet.class)
                .addMappings(staticResourcesMappings);
        DeploymentInfo deploymentInfo = Servlets.deployment()
                .addServletContainerInitalizer(sciInfo)
                .setClassLoader(this.getClass().getClassLoader())
                .setContextPath("/")
                .setDefaultEncoding("UTF-8")
                .setDeploymentName(webAppName)
                .setResourceManager(new ClassPathResourceManager(this.getClass().getClassLoader()))
                .addServlet(defaultServlet);
        deploymentManager = Servlets.defaultContainer().addDeployment(deploymentInfo);
        deploymentManager.deploy();

        HttpHandler httpHandler = deploymentManager.start();

        undertowServer = Undertow.builder()
                .addHttpListener(port, "0.0.0.0")
                .setHandler(httpHandler)
                .build();

        undertowServer.start();
        log.info("Undertow web server started on port {}", port);

        return this;
    }

    @Override
    public void destroy() throws Exception {
        log.info("Stopping Undertow web server on port {}", port);
        undertowServer.stop();
        deploymentManager.stop();
        deploymentManager.undeploy();
        log.info("Undertow web server on port {} stopped", port);
    }
}
