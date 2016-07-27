package io.foliage.api.web

import com.google.common.collect.Lists
import io.undertow.Undertow
import io.undertow.server.handlers.resource.ClassPathResourceManager
import io.undertow.servlet.Servlets
import io.undertow.servlet.api.DeploymentManager
import io.undertow.servlet.api.ServletContainerInitializerInfo
import io.undertow.servlet.handlers.DefaultServlet
import io.undertow.servlet.util.ImmediateInstanceFactory
import io.foliage.utils.loggerFor
import org.springframework.beans.factory.DisposableBean
import java.io.IOException
import java.util.*
import javax.servlet.ServletException

class WebServer(val webAppName: String, val port: Int) : DisposableBean {

    private val logger = loggerFor<WebServer>()
    private val staticResourcesMappings =
            Lists.newArrayList("*.css", "*.js", "*.ico", "*.gif", "*.jpg", "*.jpeg", "*.png")

    private var undertowServer: Undertow? = null
    private var deploymentManager: DeploymentManager? = null

    @Throws(IOException::class, ServletException::class)
    fun start(): WebServer {
        val instanceFactory = ImmediateInstanceFactory(WebServletContainerInitializer())
        val sciInfo = ServletContainerInitializerInfo(
                WebServletContainerInitializer::class.java, instanceFactory, HashSet<Class<*>>())

        val defaultServlet = Servlets
                .servlet("default", DefaultServlet::class.java)
                .addMappings(staticResourcesMappings)
        val deploymentInfo = Servlets
                .deployment()
                .addServletContainerInitalizer(sciInfo)
                .setClassLoader(this.javaClass.classLoader)
                .setContextPath("/")
                .setDefaultEncoding("UTF-8")
                .setDeploymentName(webAppName)
                .setResourceManager(ClassPathResourceManager(this.javaClass.classLoader))
                .addServlet(defaultServlet)
        deploymentManager = Servlets.defaultContainer().addDeployment(deploymentInfo)
        deploymentManager!!.deploy()

        val httpHandler = deploymentManager!!.start()

        undertowServer = Undertow
                .builder()
                .addHttpListener(port, "0.0.0.0")
                .setHandler(httpHandler)
                .build()

        undertowServer!!.start()
        logger.info("Undertow web server started on port {}", port)

        return this
    }

    @Throws(Exception::class)
    override fun destroy() {
        logger.info("Stopping Undertow web server on port {}", port)
        undertowServer!!.stop()
        deploymentManager!!.stop()
        deploymentManager!!.undeploy()
        logger.info("Undertow web server on port {} stopped", port)
    }
}