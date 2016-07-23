package org.lavenderx.foliage.api.web

import org.lavenderx.foliage.api.config.ApiConfig
import org.lavenderx.foliage.api.config.WebConfig
import org.lavenderx.foliage.nettyrpc.utils.loggerFor
import org.springframework.web.context.ContextLoaderListener
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.util.IntrospectorCleanupListener
import javax.servlet.ServletContainerInitializer
import javax.servlet.ServletContext
import javax.servlet.ServletException

class WebServletContainerInitializer : ServletContainerInitializer {

    private val logger = loggerFor<WebServletContainerInitializer>()

    @Throws(ServletException::class)
    override fun onStartup(webAppInitializerClasses: Set<Class<*>>, servletContext: ServletContext) {
        // Create the root appContext
        val rootContext = AnnotationConfigWebApplicationContext()
        rootContext.register(ApiConfig::class.java)
        // Since we registered RootConfig instead of passing it to the constructor
//        rootContext.refresh()

        // Manage the lifecycle of the root appContext
        servletContext.addListener(ContextLoaderListener(rootContext))
        servletContext.addListener(IntrospectorCleanupListener())
        servletContext.setInitParameter("defaultHtmlEscape", "true")

        // Load config for the Dispatcher servlet
        val dispatcherContext = AnnotationConfigWebApplicationContext()
        dispatcherContext.register(WebConfig::class.java)

        // The main Spring MVC servlet
        val appServlet = servletContext.addServlet("foliage", DispatcherServlet(dispatcherContext))
        appServlet.setLoadOnStartup(1)
        appServlet.addMapping("/*")

        // Setting EncodingFilter
        val encodingFilterRegistration = servletContext.addFilter("encodingFilter", CharacterEncodingFilter())
        encodingFilterRegistration.setInitParameter("encoding", "UTF-8")
        encodingFilterRegistration.setInitParameter("forceEncoding", "true")
        encodingFilterRegistration.addMappingForUrlPatterns(null, true, "/*")

        val mappingConflicts = appServlet.addMapping("/")
        if (!mappingConflicts.isEmpty()) {
            for (str in mappingConflicts) {
                logger.error("Mapping conflict: {}", str)
            }
        }
    }
}