package org.lavenderx.foliage.api.config;

import org.lavenderx.foliage.nettyrpc.config.RootConfig;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * <a href="http://joshlong.com/jl/blogPost/simplified_web_configuration_with_spring.html"></a>
 * <a href="http://www.kubrynski.com/2014/01/understanding-spring-web-initialization.html"></a>
 */
public class DispatcherServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{RootConfig.class, ApiConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{WebConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }
}
