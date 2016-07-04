package org.lavenderx.foliage.api.web;

import lombok.extern.slf4j.Slf4j;
import org.lavenderx.foliage.api.config.ApiConfig;
import org.lavenderx.foliage.api.config.WebConfig;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.util.IntrospectorCleanupListener;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.Set;

@Slf4j
public class WebServletContainerInitializer implements ServletContainerInitializer {

    @Override
    public void onStartup(Set<Class<?>> webAppInitializerClasses, ServletContext servletContext)
            throws ServletException {
        // Create the root appContext
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(ApiConfig.class);
        // Since we registered RootConfig instead of passing it to the constructor
        rootContext.refresh();

        // Manage the lifecycle of the root appContext
        servletContext.addListener(new ContextLoaderListener(rootContext));
        servletContext.addListener(new IntrospectorCleanupListener());
        servletContext.setInitParameter("defaultHtmlEscape", "true");

        // Load config for the Dispatcher servlet
        AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
        dispatcherContext.register(WebConfig.class);

        // The main Spring MVC servlet
        ServletRegistration.Dynamic appServlet = servletContext.addServlet("foliage", new DispatcherServlet(dispatcherContext));
        appServlet.setLoadOnStartup(1);
        appServlet.addMapping("/*");

        // Setting EncodingFilter
        FilterRegistration.Dynamic encodingFilterRegistration = servletContext.addFilter("encodingFilter", new CharacterEncodingFilter());
        encodingFilterRegistration.setInitParameter("encoding", "UTF-8");
        encodingFilterRegistration.setInitParameter("forceEncoding", "true");
        encodingFilterRegistration.addMappingForUrlPatterns(null, true, "/*");

        Set<String> mappingConflicts = appServlet.addMapping("/");
        if (!mappingConflicts.isEmpty()) {
            for (String str : mappingConflicts) {
                log.error("Mapping conflict: {}", str);
            }
        }
    }
}
