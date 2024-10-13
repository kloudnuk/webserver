package com.kloudnuk.webserver.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan(basePackages = {"com.kloudnuk.webserver"})
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public Validator validator() {
        final var validator = new LocalValidatorFactoryBean();
        return validator;
    }

    @Override
    public Validator getValidator() {
        return validator();
    }
}
