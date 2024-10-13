package com.kloudnuk.webserver;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import com.kloudnuk.webserver.configurations.SecurityConfig;
import com.kloudnuk.webserver.configurations.ServiceConfig;
import com.kloudnuk.webserver.configurations.TransactionConfig;
import com.kloudnuk.webserver.configurations.WebConfig;

public class WebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] {SecurityConfig.class, ServiceConfig.class, TransactionConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] {WebConfig.class};
    }

    @Override
    @SuppressWarnings("null")
    protected String[] getServletMappings() {
        return new String[] {"/"};
    }
}
