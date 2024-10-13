package com.kloudnuk.webserver.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.context.annotation.Bean;

import com.kloudnuk.webserver.services.S3DistributionProvider;
import com.kloudnuk.webserver.services.UserManager;
import com.kloudnuk.webserver.services.MongoDbSimpleProvider;
import com.kloudnuk.webserver.services.api.IDataStoreProvider;
import com.kloudnuk.webserver.services.api.IDistributionProvider;
import com.kloudnuk.webserver.services.api.IUserManager;

@Configuration
@ComponentScan(basePackages = {"com.kloudnuk.webserver.services"})
public class ServiceConfig {

    @Bean("APP_DIR")
    public String appDir() {
        return new String("/nuk/");
    }

    @Bean("PACKAGE_DIR")
    public String packageDir() {
        return new String("/nuk/.packages/");
    }

    @Bean(destroyMethod = "close")
    public IDistributionProvider distributor() {
        return new S3DistributionProvider();
    }

    @Bean
    public IDataStoreProvider dsprovider() {
        return new MongoDbSimpleProvider();
    }

    @Bean
    public IUserManager usermgr() {
        return new UserManager();
    }
}
